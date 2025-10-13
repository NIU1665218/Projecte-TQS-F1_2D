package es.uab.tqs.f1_2D.model;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.event.KeyEvent;

public class Car
{
    private double x, y;
    private double angle;
    private double velocity;
    private double maxVelocity;
    private double acceleration;
    private double friction;
    private double backwardsMaxVelocity;
    private int mapHeight;
    private int mapWidth;
    private BufferedImage sprite;

    public Car(double x, double y, double angle, double velocity, double maxVelocity, double backwardsMaxVelocity, double acceleration, int mapHeight, int mapWidth) 
    {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
        this.maxVelocity = maxVelocity;
        this.backwardsMaxVelocity = backwardsMaxVelocity;
        this.acceleration = acceleration;
        this.friction = 0.99;
        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;
    }

    public Boolean movement(int inputKey)
    {
        switch (inputKey) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                velocity += acceleration;
                if (velocity > maxVelocity)
                    velocity = maxVelocity;
                break;

            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                velocity -= acceleration;
                if (velocity < backwardsMaxVelocity)
                    velocity = backwardsMaxVelocity;
                break;
            
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                angle -= 15;
                break;

            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                angle += 15;
                break;
        
            default:
                return false;
        }
        return true;
    }

    public boolean trackLimits(BufferedImage mapImage)
    {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) return true;
        int color = mapImage.getRGB((int)x, (int)y);
        return color != Color.GRAY.getRGB(); 
    }

    public void update()
    {   
        velocity *= friction;

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