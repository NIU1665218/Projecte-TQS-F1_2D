package es.uab.tqs.f1_2D.model;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.Color;



class CarTest
{
    Car car;

    //Constants
    public static final double maxVelocity = 10;
    public static final double backwardsMaxVelocity = -5;
    public static final double acceleration = 2;
    public static final int angleMovement = 15;
    public static final int testMapHeight = 400;
    public static final int testMapWidth = 400;

    @BeforeEach
    public void setUp()
    {
        car = new Car(0,0,0f,0, maxVelocity, backwardsMaxVelocity, acceleration, testMapHeight, testMapWidth);
    }

    /* 
      =============================================================================
        TEST DRIVEN DEVELOPMENT
      =============================================================================
    */
    @Test 
    public void GettersSetterTest()
    {
        //Test set i get de la coordenada X del cotxe sobre el mapa
        car.setX(4);
        assertEquals(4, car.getX());
        car.setX(500);
        assertEquals(500, car.getX());
        car.setX(-1);
        assertTrue(4 != car.getX());

        //Test set i get de la coordenada Y del cotxe sobre el mapa
        car.setY(5);
        assertEquals(5, car.getY());
        car.setY(600);
        assertEquals(600, car.getY());
        car.setY(-1000);
        assertTrue(4 != car.getY());

        //Test set i get de l'angle del cotxe pels girs
        car.setAngle(45);
        assertEquals(45, car.getAngle());
        car.setAngle(0);
        assertEquals(0, car.getAngle());
        car.setAngle(364);
        assertTrue(30 != car.getAngle());

        //Test set i get de la velocitat
        car.setVelocity(5);
        assertEquals(5, car.getVelocity());
        car.setVelocity(195);
        assertEquals(195, car.getVelocity());
        car.setVelocity(100);
        assertTrue(99 != car.getVelocity());
    }

    @Test
    public void MovimentTest()
    {
        //Test moviment endavant amb tecles W o flecha superior
        car.movement(KeyEvent.VK_W);
        assertEquals(2, car.getVelocity());
        car.movement(KeyEvent.VK_UP);
        assertEquals(4, car.getVelocity());

        //Test moviment parar/ fre amb tecles S o flecha inferior
        car.setVelocity(8);
        car.movement(KeyEvent.VK_S);
        assertEquals(6, car.getVelocity());
        car.movement(KeyEvent.VK_DOWN);
        assertEquals(4, car.getVelocity());

        //Test moviment enrere (car.getVelocity <= 0)
        car.setVelocity(0);
        car.movement(KeyEvent.VK_S);
        assertEquals(-2, car.getVelocity());
        car.movement(KeyEvent.VK_S);
        assertEquals(-4, car.getVelocity());
        car.setVelocity(0);
        car.movement(KeyEvent.VK_DOWN);
        assertEquals(-2, car.getVelocity());
        car.movement(KeyEvent.VK_DOWN);
        assertEquals(-4, car.getVelocity());

        //Test moviment lateral amb tecles A/D o fleches laterals
        car.movement(KeyEvent.VK_A);
        assertEquals(-15, car.getAngle());
        car.movement(KeyEvent.VK_LEFT);
        assertEquals(-30, car.getAngle());
        car.movement(KeyEvent.VK_D);
        assertEquals(-15, car.getAngle());
        car.movement(KeyEvent.VK_RIGHT);
        assertEquals(0, car.getAngle());

    }

    @Test
    public void trackLimitsTest()
    {
        BufferedImage map = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        //Test que comprova si el cotxe es troba fora de pista
        map.setRGB(5, 5, Color.GREEN.getRGB());
        car.setX(5);
        car.setY(5);
        assertTrue(car.trackLimits(map));

        //Test que comprova si el cotxe es troba dins de pista
        map.setRGB(9, 9, Color.GRAY.getRGB());
        car.setX(9);
        car.setY(9);
        assertFalse(car.trackLimits(map));
    }

    /* 
      =============================================================================
        EQUIVALENT PARTITIONS
      =============================================================================
    */
    @Test
    public void settersGettersPartitionTest()
    {
        //1. Positive values 2. Limit Value 3. Negative Value

        //X coord 
        car.setX(2);
        assertEquals(2, car.getX());
        car.setX(0);
        assertEquals(0, car.getX());
        car.setX(testMapHeight);
        assertEquals(testMapHeight, car.getX());
        car.setX(-2);
        assertEquals(-2, car.getX());

        //Y coord 
        car.setY(2);
        assertEquals(2, car.getY());
        car.setY(0);
        assertEquals(0, car.getY());
        car.setY(testMapWidth);
        assertEquals(testMapWidth, car.getY());
        car.setY(-2);
        assertEquals(-2, car.getY());

        //Angle Setter 
        car.setAngle(45);
        assertEquals(45, car.getAngle());
        car.setAngle(0);
        assertEquals(0, car.getAngle());
        car.setAngle(360);
        assertEquals(360, car.getAngle());
        car.setAngle(-45);
        assertEquals(-45, car.getAngle());

        //Velocity Setter
        car.setVelocity(2);
        assertEquals(2, car.getVelocity());
        car.setVelocity(0);
        assertEquals(0, car.getVelocity());
        car.setVelocity(10);
        assertEquals(10, car.getVelocity());
        car.setVelocity(-5);
        assertEquals(-5, car.getVelocity());

    }



    @Test 
    public void forwardMovementPartitionTest()
    {
        //Test for forward movement using W or UP
        car.setVelocity(backwardsMaxVelocity);
        for(double i = backwardsMaxVelocity + acceleration; i < maxVelocity; i+= acceleration)
        {
            car.movement(KeyEvent.VK_W);
            assertEquals(i, car.getVelocity());      
        }

        car.setVelocity(backwardsMaxVelocity);
        for(double i = backwardsMaxVelocity + acceleration; i < maxVelocity; i+= acceleration)
        {
            car.movement(KeyEvent.VK_UP);
            assertEquals(i, car.getVelocity());
        }
    }

    @Test 
    public void backwardsMovementPartitionTest()
    {
        //Test for backswards movement using S or DOWN
        car.setVelocity(maxVelocity);
        for(double i = maxVelocity - acceleration; i > backwardsMaxVelocity; i-= acceleration)
        {
            car.movement(KeyEvent.VK_S);
            assertEquals(i, car.getVelocity());         
        }

        car.setVelocity(maxVelocity);
        for(double i = maxVelocity - acceleration; i > backwardsMaxVelocity; i-= acceleration)
        {
            car.movement(KeyEvent.VK_DOWN);
            assertEquals(i, car.getVelocity());
        }
    }

    @Test 
    public void LeftMovementPartitionTest()
    {
        //Test for left movement using A or LEFT
        car.setAngle(360);
        for(double i = 360 - angleMovement; i > 0; i-= angleMovement)
        {
            car.movement(KeyEvent.VK_A);
            assertEquals(i, car.getAngle());         
        }

        car.setAngle(360);
        for(double i = 360 - angleMovement; i > 0; i-= angleMovement)
        {
            car.movement(KeyEvent.VK_LEFT);
            assertEquals(i, car.getAngle());  
        }
    }

    @Test 
    public void RightMovementPartitionTest()
    {
        //Test for right movement using D or RIGHT
        car.setAngle(0);
        for(double i = 0 + angleMovement; i < 360; i+= angleMovement)
        {
           car.movement(KeyEvent.VK_D);
            assertEquals(i, car.getAngle());         
        }

        car.setAngle(0);
        for(double i = 0 + angleMovement; i < 360; i+= angleMovement)
        {
            car.movement(KeyEvent.VK_RIGHT);
            assertEquals(i, car.getAngle());
        }
    }

    @Test 
    public void NoMovementKeyPartitionTest()
    {
        //Test for other keys not binded
        assertFalse(car.movement(KeyEvent.VK_F));
        assertFalse(car.movement(KeyEvent.VK_E));
        //...
    }

    @Test 
    public void trackLimitsPartitionTest()
    {
        BufferedImage map = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        //Test que comprova si el cotxe es troba fora de pista
        map.setRGB(5, 5, Color.GREEN.getRGB());
        car.setX(5);
        car.setY(5);
        assertTrue(car.trackLimits(map));

        map.setRGB(9, 9, Color.GRAY.getRGB());
        car.setX(9);
        car.setY(9);
        assertFalse(car.trackLimits(map));

        car.setX(999);
        car.setY(999);
        assertTrue(car.trackLimits(map));
    }

    @Test
    public void updatePartitionTest()
    {
        //Positive Velocity
        car.setX(50);
        car.setY(50);
        car.setAngle(0);
        car.setVelocity(10);
        car.update();
        assertTrue(car.getX() > 50);
        assertEquals(50, car.getY());

        //Negative Velocity
        car.setX(50);
        car.setY(50);
        car.setAngle(0);
        car.setVelocity(-5);
        car.update();
        assertTrue(car.getX() < 50);
        assertEquals(50, car.getY());

        //Null Velocity
        car.setX(50);
        car.setY(50);
        car.setAngle(0);
        car.setVelocity(0.001);
        car.update();
        assertTrue(car.getX() == 50);
        assertEquals(50, car.getY());

        //Out of bounds
        car.setX(-10);
        car.setY(testMapHeight);
        car.setAngle(0);
        car.setVelocity(-5);
        car.update();
        assertEquals(0, car.getX());
        assertEquals(testMapHeight, car.getY());
    }


}