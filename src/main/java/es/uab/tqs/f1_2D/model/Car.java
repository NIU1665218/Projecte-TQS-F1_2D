package es.uab.tqs.f1_2D.model;

import java.awt.image.BufferedImage;
import java.util.Set;
import java.awt.Color;
import java.awt.event.KeyEvent;

public class Car
{
    private double x, y;
    private double angle;
    private double velocity;
    private double maxVelocity;
    private double acceleration;
    private double backwardsAcceleration;
    private double friction;
    private double offtrackFriction;
    private double backwardsMaxVelocity;
    private int mapHeight;
    private int mapWidth;
    private BufferedImage sprite;

    public Car(double x, double y, double angle, double velocity, double maxVelocity, double backwardsMaxVelocity, double acceleration, double backwardsAcceleration, int mapHeight, int mapWidth) 
    {
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
    }

    public Boolean movement(Set<Integer> keys)
    {
        boolean moved = false;

        if (keys.contains(KeyEvent.VK_W) || keys.contains(KeyEvent.VK_UP)) {
            velocity += acceleration;
            if (velocity > maxVelocity) velocity = maxVelocity;
            moved = true;
        } 
        if (keys.contains(KeyEvent.VK_S) || keys.contains(KeyEvent.VK_DOWN)) {
            velocity -= backwardsAcceleration;
            if (velocity < backwardsMaxVelocity) velocity = backwardsMaxVelocity;
            moved = true;
        }

        // Rotación
        if (keys.contains(KeyEvent.VK_A) || keys.contains(KeyEvent.VK_LEFT)) {
            angle -= 5;
            moved = true;
        } 
        if (keys.contains(KeyEvent.VK_D) || keys.contains(KeyEvent.VK_RIGHT)) {
            angle += 5;
            moved = true;
        }

        return moved;
    }

    public boolean trackLimits(BufferedImage mapImage)
    {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) return true;
        int color = mapImage.getRGB((int)x + 50, (int)y + 50);
        Color c = new Color(color);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        int targetR = 73;
        int targetG = 127;
        int targetB = 78;
        int tolerance = 5; 

        boolean similarToGreen =
            Math.abs(r - targetR) <= tolerance &&
            Math.abs(g - targetG) <= tolerance &&
            Math.abs(b - targetB) <= tolerance;

        return similarToGreen; 
    }

    public void update(BufferedImage mapImage)
    {   
        if(trackLimits(mapImage)) {
            velocity *= offtrackFriction;
        }
        else
        {
            velocity *= friction;
        }

        if(Math.abs(velocity) < 0.1)
            velocity = 0;

        double rad = Math.toRadians(angle);
        x += Math.cos(rad) * velocity;
        y += Math.sin(rad) * velocity;

        if(x<0) x = 0;
        if(y<0) y = 0;
        if(x > mapWidth) x = mapWidth;
        if(y > mapHeight) y = mapHeight;
    }

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