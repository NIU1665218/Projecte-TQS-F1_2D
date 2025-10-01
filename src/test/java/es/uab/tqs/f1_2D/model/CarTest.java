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

    @BeforeEach
    public void setUp()
    {
        car = new Car(0,0,0f,0,200,0,400,400);
    }

    @Test 
    public void GettersSetterTest()
    {
        //Test set i get de la coordenada X del cotxe sobre el mapa
        car.setX(4);
        assertEquals(4, car.getX());
        car.setX(500);
        assertEquals(500, car.getX());
        car.setX(-1);
        assertFalse(4 != car.getX());

        //Test set i get de la coordenada Y del cotxe sobre el mapa
        car.setY(5);
        assertEquals(5, car.getY());
        car.setY(600);
        assertEquals(600, car.getY());
        car.setY(-1000);
        assertFalse(4 != car.getY());

        //Test set i get de l'angle del cotxe pels girs
        car.setAngle(45);
        assertEquals(45, car.getAngle());
        car.setAngle(0);
        assertEquals(0, car.getAngle());
        car.setAngle(364);
        assertFalse(30 != car.getAngle());

        //Test set i get de la velocitat
        car.setVelocity(5);
        assertEquals(5, car.getVelocity());
        car.setVelocity(195);
        assertEquals(195, car.getVelocity());
        car.setVelocity(100);
        assertFalse(99 != car.getVelocity());

        car.setMaxVelocity(10);
        assertEquals(10, car.getMaxVelocity());
        car.setMaxVelocity(189);
        assertEquals(189, car.getMaxVelocity());
        car.setMaxVelocity(101);
        assertFalse(100 != car.getMaxVelocity());
    }

    @Test
    public void MovimentTest()
    {
        //Test moviment endavant amb tecles W o flecha superior
        car.movement(KeyEvent.VK_W);
        assertEquals(10, car.getVelocity());
        car.movement(KeyEvent.VK_UP);
        assertEquals(20, car.getVelocity());

        //Test moviment parar/ fre amb tecles S o flecha inferior
        car.setVelocity(40);
        car.movement(KeyEvent.VK_S);
        assertEquals(30, car.getVelocity());
        car.movement(KeyEvent.VK_DOWN);
        assertEquals(20, car.getVelocity());

        //Test moviment enrere (car.getVelocity <= 0)
        car.setVelocity(0);
        car.movement(KeyEvent.VK_S);
        assertEquals(-10, car.getVelocity());
        car.movement(KeyEvent.VK_S);
        assertEquals(-20, car.getVelocity());
        car.setVelocity(0);
        car.movement(KeyEvent.VK_DOWN);
        assertEquals(-10, car.getVelocity());
        car.movement(KeyEvent.VK_DOWN);
        assertEquals(-20, car.getVelocity());

        //Test moviment lateral amb tecles A/D o fleches laterals
        car.movement(KeyEvent.VK_A);
        assertEquals(-5, car.getAngle());
        car.movement(KeyEvent.VK_LEFT);
        assertEquals(-10, car.getAngle());
        car.movement(KeyEvent.VK_D);
        assertEquals(-5, car.getAngle());
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
        car.setX(5);
        car.setY(5);
        assertFalse(car.trackLimits(map));
    }


}