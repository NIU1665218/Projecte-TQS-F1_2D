package es.uab.tqs.f1_2D.model;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.event.KeyEvent;

import java.util.Set;

public class Car
{
    protected double x, y;
    protected double angle;
    protected double velocity;
    protected double maxVelocity;
    protected double acceleration;
    protected double backwardsAcceleration;
    protected double friction;
    protected double offtrackFriction;
    protected double backwardsMaxVelocity;
    protected int mapHeight;
    protected int mapWidth;
    protected BufferedImage sprite;
    private double slipstreamBoost;

    public Car(double x, double y, double angle, double velocity, double maxVelocity, double backwardsMaxVelocity, double acceleration, double backwardsAcceleration, int mapHeight, int mapWidth) 
    {
        //Inicialización de todas las variables
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
        this.maxVelocity = maxVelocity;
        this.backwardsMaxVelocity = backwardsMaxVelocity;
        this.acceleration = acceleration;
        this.backwardsAcceleration = backwardsAcceleration;
        this.friction = 0.99;
        this.offtrackFriction = 0.7;
        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;
        this.slipstreamBoost = 0;
    }

    //Función para controlar el movimiento en base a las teclas 
    public Boolean movement(Set<Integer> keys)
    {
        boolean moved = false;
        //Calcular maxVelocity + el boost del slipstream
        double effectiveMaxVelocity = getEffectiveMaxVelocity();

        //Si contiene teclas de avance, aumentar la velocidad o mantener en caso de máximos
        if (keys.contains(KeyEvent.VK_W) || keys.contains(KeyEvent.VK_UP)) {
            velocity += acceleration;
            if (velocity > effectiveMaxVelocity) velocity = effectiveMaxVelocity;
            moved = true;
        } 

        //Si contiene teclas de retroceso/freno, disminuir la velocidad o mantener en caso de máximos
        if (keys.contains(KeyEvent.VK_S) || keys.contains(KeyEvent.VK_DOWN)) {
            velocity -= backwardsAcceleration;
            if (velocity < backwardsMaxVelocity) velocity = backwardsMaxVelocity;
            moved = true;
        }

        //Si contiene teclas de rotación hacia la izquierda, disminuir el angulo
        if (keys.contains(KeyEvent.VK_A) || keys.contains(KeyEvent.VK_LEFT)) {
            angle -= 5;
            moved = true;
        } 

        //Si contiene teclas de rotación hacia la derecha, disminuir el angulo
        if (keys.contains(KeyEvent.VK_D) || keys.contains(KeyEvent.VK_RIGHT)) {
            angle += 5;
            moved = true;
        }

        return moved;
    }

    //Comprobar si el coche se encuentra fuera de los límites de pista
    public boolean trackLimits(BufferedImage mapImage)
    {
        //Si se encuentra fuera del mapa literalmente, devolver true
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) return true;

        //Calcular el centro del sprite
        int xi = (int)x + 40;
        int yi = (int)y + 40;
        xi = Math.min(Math.max(xi, 0), mapImage.getWidth() - 1);
        yi = Math.min(Math.max(yi, 0), mapImage.getHeight() -1);

        //Obtener el color del mapa de colisiones en la posición actual
        int color = mapImage.getRGB(xi, yi);
        Color c = new Color(color);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        int targetR = 73;
        int targetG = 127;
        int targetB = 78;
        int tolerance = 20; 

        //Si el color es similar al verde, significa que se encuentra fuera de pista
        boolean similarToGreen =
            Math.abs(r - targetR) <= tolerance &&
            Math.abs(g - targetG) <= tolerance &&
            Math.abs(b - targetB) <= tolerance;

        targetR = 0;
        targetG = 0;
        targetB = 255;

        //Si el color es similar al azul, significa que está encima de una pared invisible (fuera de pista)
        boolean similarToBlue =
            Math.abs(r - targetR) <= tolerance &&
            Math.abs(g - targetG) <= tolerance &&
            Math.abs(b - targetB) <= tolerance;

        return similarToGreen || similarToBlue || color == Color.GREEN.getRGB();
    }

    //Comprobar si el coche ha chocado contra una pared invisible para evitar saltos en pista
    public boolean invisibleWalls(BufferedImage mapImage)
    {
        //Si se encuentra fuera del mapa literalmente, devolver true
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) return true;

        //Calcular el centro del sprite del coche
        int xi = (int)x + 40;
        int yi = (int)y + 40;
        xi = Math.min(Math.max(xi, 0), mapImage.getWidth() - 1);
        yi = Math.min(Math.max(yi, 0), mapImage.getHeight() -1);

        //Obtener el color del mapa de colisiones en la posición actual
        int color = mapImage.getRGB(xi, yi);
        Color c = new Color(color);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        int targetR = 0;
        int targetG = 0;
        int targetB = 255;
        int tolerance = 20;

        //Si el color es similar al azul, ha encontrado una pared invisible
        boolean similarToBlue =
            Math.abs(r - targetR) <= tolerance &&
            Math.abs(g - targetG) <= tolerance &&
            Math.abs(b - targetB) <= tolerance;

        return similarToBlue;
    }

    //Función principal para hacer update de la posición
    public void update(BufferedImage mapImage)
    {   
        //Evitar que el coche se vaya fuera del mapa
        correctOutOfMapBounds();

        //Si el coche se encuentra offtrack, aplicar una reducción de velocidad
        if(trackLimits(mapImage)) 
        {
            velocity *= offtrackFriction;
        }
        //Si está en pista, aplicar una fricción baja
        else
        {
            velocity *= friction;
        }

        //Redondear números cercanos al 0
        if(Math.abs(velocity) < 0.1) velocity = 0;

        //Calcular la nueva posición 
        double rad = Math.toRadians(angle);
        double oldx = x;
        double oldy = y;
        x += Math.cos(rad) * velocity;
        y += Math.sin(rad) * velocity;

        //Comprobar que el coche no se salga del mapa
        correctOutOfMapBounds();

        //Si detecta que se encuentra una pared invisible, no actualizar posición
        if(invisibleWalls(mapImage)) 
        {
            x = oldx;
            y = oldy;
            velocity = 0;
            return;
        }

        //Comprobar que el coche no se salga del mapa una vez actualizado del todo
        correctOutOfMapBounds();
    }

    //Si el coche se encuentra en posición de slipstream, aplicarla si la vuelta es mayor a la primera
    public void applySlipstream(boolean inSlipstream, int currentLap) 
    {
        if (currentLap >= 2 && inSlipstream) 
        {
            slipstreamBoost = 5.0;
        } 
        else 
        {
            slipstreamBoost = 0;
        }
    }
    
    //Cálculo de la velocidad máxima
    public double getEffectiveMaxVelocity() 
    {
        return maxVelocity + slipstreamBoost;
    }

    //Comprobar que el coche no se salga de los límites del mapa
    public void correctOutOfMapBounds()
    {
        if(x<0) x = 0;
        if(y<0) y = 0;
        if(x > mapWidth) x = mapWidth;
        if(y > mapHeight) y = mapHeight;
    }

    //Setters/Getters
    public double getX() {return x;}
    public double getY() {return y;}
    public void setX(double newX) {this.x = newX;}
    public void setY(double newY) {this.y = newY;}

    public double getAngle() {return angle;}
    public void setAngle(double newAngle) { this.angle = newAngle;}

    public double getVelocity() {return velocity;}
    public void setVelocity(double newVelocity) { this.velocity = newVelocity;}

    public void setSprite(BufferedImage sprite) {this.sprite = sprite;}
    public BufferedImage getSprite() { return sprite;}

}