

public class Car
{
    private int x, y;
    private float angle;
    private int velocity;
    private int maxVelocity;
    private int acceleration;

    private int mapHeight;
    private int mapWidth;

    
    public Car() {};

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

    public bool movement(char inputKey);

    public bool trackLimits();

    public void Update();


}