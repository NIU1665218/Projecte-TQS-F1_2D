package es.uab.tqs.f1_2D.controlador;

import es.uab.tqs.f1_2D.model.Car;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.event.KeyEvent;

public class CarControllerTest {

    private Car car;
    private CarController controller;

    @BeforeEach
    public void setUp() {
        car = new Car(100, 100, 0, 0, 10, -5, 1, 500, 500);
        controller = new CarController(car);
    }

    /* 
      =============================================================================
        TEST DRIVEN DEVELOPMENT
      =============================================================================
    */

    @Test
    public void testProcessInput() {
        
        //Valid Key
        controller.processInput(KeyEvent.VK_W);
        assertTrue(car.getVelocity() > 0);

        //Invalid Key
        car.setVelocity(0);
        controller.processInput(999); 
        assertEquals(0, car.getVelocity());
    }

    /* 
      =============================================================================
        EQUIVALENT PARTITIONS
      =============================================================================
    */

    // W | UP
    @Test
    public void testAccelerate() {
        controller.processInput(KeyEvent.VK_W);
        assertTrue(car.getVelocity() > 0);
    }

    // S | DOWN
    @Test
    public void testDecrease() {
        car.setVelocity(5);
        controller.processInput(KeyEvent.VK_S);
        assertTrue(car.getVelocity() < 5);
    }

    // A | LEFT
    @Test
    public void testTurnA() {
        double initialAngle = car.getAngle();
        controller.processInput(KeyEvent.VK_A);
        assertTrue(car.getAngle() < initialAngle);
    }

    // A | LEFT
    @Test
    public void testTurnLeft() {
        double initialAngle = car.getAngle();
        controller.processInput(KeyEvent.VK_LEFT);
        assertTrue(car.getAngle() < initialAngle);
    }

    // D | RIGHT
    @Test
    public void testTurnD() {
        double initialAngle = car.getAngle();
        controller.processInput(KeyEvent.VK_D);
        assertTrue(car.getAngle() > initialAngle);
    }

    // D | RIGHT
    @Test
    public void testTurnRight() {
        double initialAngle = car.getAngle();
        controller.processInput(KeyEvent.VK_RIGHT);
        assertTrue(car.getAngle() > initialAngle);
    }

    // Another Key not bound
    @Test
    public void testInvalidKeyDoesNothing() {
        double initialVelocity = car.getVelocity();
        double initialAngle = car.getAngle();
        controller.processInput(999);
        assertEquals(initialVelocity, car.getVelocity());
        assertEquals(initialAngle, car.getAngle());
    }

    // Update Movement
    @Test
    public void testUpdateMovesCar() {
        car.setVelocity(5);
        controller.processInput(KeyEvent.VK_W);
        assertTrue(car.getX() > 100);
    }

    // Velocity not exceeded
    @Test
    public void testVelocityMax() {
        car.setVelocity(10);
        controller.processInput(KeyEvent.VK_W);
        assertTrue(car.getVelocity() <= 10 && car.getVelocity() >=9.9);
    }

    // Backwards Velocity not exceeded
    @Test
    public void testVelocityBackwardsMax() {
        car.setVelocity(-5);
        controller.processInput(KeyEvent.VK_S);
        assertTrue(car.getVelocity() >= -5 && car.getVelocity() <= -4.9);
    }
}
