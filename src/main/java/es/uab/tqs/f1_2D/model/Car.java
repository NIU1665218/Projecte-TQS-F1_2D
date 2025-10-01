package es.uab.tqs.f1_2D.model;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class Car
{
    private int x, y;
    private float angle;
    private int velocity;
    private int maxVelocity;
    private int acceleration;

    public Car(int x, int y, float angle, int velocity, int maxVelocity, int acceleration, int mapHeight, int mapWidth) 
    {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
        this.maxVelocity = maxVelocity;
        this.acceleration = acceleration;
    }

    public Boolean movement(int inputKey)
    {
        return true;
    }

    public boolean trackLimits(BufferedImage mapImage)
    {
        return true;
    }

    public void Update()
    {
        return;
    }

    public int getX() {return x;}
    public int getY() {return y;}
    public void setX(int newX) {x = newX;}
    public void setY(int newY) {y = newY;}

    public float getAngle() {return angle;}
    public void setAngle(float newAngle) { angle = newAngle;}

    public int getMaxVelocity() { return maxVelocity;}
    public void setMaxVelocity(int newMaxVelocity) { maxVelocity = newMaxVelocity;}
    public int getVelocity() {return velocity;}
    public void setVelocity(int newVelocity) { velocity = newVelocity;}

}