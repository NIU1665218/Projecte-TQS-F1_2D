package es.uab.tqs.f1_2D.model;

public class Car
{
    private int x, y;
    private float angle;
    private int velocity;
    private int maxVelocity;
    private int minVelocity;

    private int acceleration;

    private int mapHeight;
    private int mapWidth;

    public Car(int x, int y, float angle, int velocity, int maxVelocity, int acceleration, int mapHeight, int mapWidth) 
    {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
        this.maxVelocity = maxVelocity;
        this.acceleration = acceleration;

        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;
    }

    public Boolean movement(char inputKey)
    {
        return true;
    }

    public Boolean trackLimits()
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

    public int getMaxVelocity() { return maxVelocity;}
    public void setMaxVelocity(int newMaxVelocity) { maxVelocity = newMaxVelocity;}
    public int getVelocity() {return velocity;}
    public void setVelocity(int newVelocity) { velocity = newVelocity;}

}