package es.uab.tqs.f1_2D.controlador;
import es.uab.tqs.f1_2D.model.Car;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.awt.event.KeyEvent;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class CarControllerTest {

    @Mock
    private Car mockCar;

    private Car car;

  
    private CarController controller;
    private CarController mockController;

    @BeforeEach
    public void setUp() {
        car = new Car(100, 100, 0, 0, 10, -5, 1, 500, 500);
        controller = new CarController(car);
        mockController = new CarController(mockCar);
    }

    /* 
      =============================================================================
        TEST DRIVEN DEVELOPMENT
      =============================================================================
    */

    @Test
    public void testProcessInput() {
        
        //Valid Key
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertTrue(car.getVelocity() > 0);

        //Invalid Key
        car.setVelocity(0);
        controller.processInput(Set.of(999)); 
        assertEquals(0, car.getVelocity());
    }

    @Test
    void testProcessMultipleInputsWD() {

        controller.processInput(Set.of(KeyEvent.VK_W, KeyEvent.VK_D));

        assertTrue(car.getVelocity() > 0);
        assertTrue(car.getAngle() > 0);
        assertTrue(car.getX() > 0);
    }

    @Test
    void testProcessMultipleInputsWA() {
        controller.processInput(Set.of(KeyEvent.VK_W, KeyEvent.VK_A));

        assertTrue(car.getVelocity() > 0);
        assertTrue(car.getAngle() < 0);
    }

    @Test
    void testProcessMultipleInputsSD() {
        controller.processInput(Set.of(KeyEvent.VK_S, KeyEvent.VK_D));

        assertTrue(car.getVelocity() < 0);
        assertTrue(car.getAngle() > 0);
    }

    @Test
    void testProcessMultipleInputsSA() {
        controller.processInput(Set.of(KeyEvent.VK_S, KeyEvent.VK_A));

        assertTrue(car.getVelocity() < 0);
        assertTrue(car.getAngle() < 0);
    }

    @Test
    void testCarState() {
        controller.processInput(Set.of(KeyEvent.VK_W));
        double xAfter = car.getX();
        double vAfter = car.getVelocity();

        controller.processInput(Set.of(KeyEvent.VK_S));
        assertNotEquals(vAfter, car.getVelocity());
        assertNotEquals(xAfter, car.getX());
    }

    @Test
    void testNoKey() {
        double xBefore = car.getX();
        double vBefore = car.getVelocity();

        controller.processInput(Set.of());

        assertEquals(xBefore, car.getX());
        assertEquals(vBefore, car.getVelocity());
    }

    /* 
      =============================================================================
        EQUIVALENT PARTITIONS
      =============================================================================
    */

    // W | UP
    @Test
    public void testAccelerate() {
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertTrue(car.getVelocity() > 0);
    }

    // S | DOWN
    @Test
    public void testDecrease() {
        car.setVelocity(5);
        controller.processInput(Set.of(KeyEvent.VK_S));
        assertTrue(car.getVelocity() < 5);
    }

    // A | LEFT
    @Test
    public void testTurnA() {
        double initialAngle = car.getAngle();
        controller.processInput(Set.of(KeyEvent.VK_A));
        assertTrue(car.getAngle() < initialAngle);
    }

    // A | LEFT
    @Test
    public void testTurnLeft() {
        double initialAngle = car.getAngle();
        controller.processInput(Set.of(KeyEvent.VK_LEFT));
        assertTrue(car.getAngle() < initialAngle);
    }

    // D | RIGHT
    @Test
    public void testTurnD() {
        double initialAngle = car.getAngle();
        controller.processInput(Set.of(KeyEvent.VK_D));
        assertTrue(car.getAngle() > initialAngle);
    }

    // D | RIGHT
    @Test
    public void testTurnRight() {
        double initialAngle = car.getAngle();
        controller.processInput(Set.of(KeyEvent.VK_RIGHT));
        assertTrue(car.getAngle() > initialAngle);
    }

    // Another Key not bound
    @Test
    public void testInvalidKeyDoesNothing() {
        double initialVelocity = car.getVelocity();
        double initialAngle = car.getAngle();
        controller.processInput(Set.of(999));
        assertEquals(initialVelocity, car.getVelocity());
        assertEquals(initialAngle, car.getAngle());
    }

    @Test
    public void testDuplicateKeys() {
        
        controller.processInput(Set.of(KeyEvent.VK_W, KeyEvent.VK_W));
        assertTrue(car.getVelocity() > 0 && car.getVelocity() < 2.5);
      
    }

    // Three Keys Movement
    @Test
    public void testThreeKeysWAD() {

        controller.processInput(Set.of(KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_D));
        
    
        assertTrue(car.getVelocity() > 0);
        assertTrue(car.getX() > 0);
        assertEquals(0, car.getAngle());
    }

    @Test
    public void testThreeKeysSAD() {
        controller.processInput(Set.of(KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D));
       
        assertTrue(car.getVelocity() < 0);
        assertTrue(car.getX() > 0);
        assertEquals(0, car.getAngle());
    }

    // Opposite Combinations
    @Test
    public void testOppositeKeysWA_SD() {
        
        controller.processInput(Set.of(KeyEvent.VK_W, KeyEvent.VK_S));
        controller.processInput(Set.of(KeyEvent.VK_A, KeyEvent.VK_D));
        
        assertEquals(0, car.getVelocity());
        assertEquals(0, car.getAngle());
    }

    // Update Movement
    @Test
    public void testUpdateMovesCar() {
        car.setVelocity(5);
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertTrue(car.getX() > 100);
    }

    // Velocity not exceeded
    @Test
    public void testVelocityMax() {
        car.setVelocity(10);
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertTrue(car.getVelocity() <= 10 && car.getVelocity() >=9.9);
    }

    // Backwards Velocity not exceeded
    @Test
    public void testVelocityBackwardsMax() {
        car.setVelocity(-5);
        controller.processInput(Set.of(KeyEvent.VK_S));
        assertTrue(car.getVelocity() >= -5 && car.getVelocity() <= -4.9);
    }

     /* 
      =============================================================================
        Pairwise Testing
      =============================================================================
    */

    @Test
    public void testPairwiseWA() {
        controller.processInput(Set.of(KeyEvent.VK_W, KeyEvent.VK_A));
        assertTrue(car.getVelocity() > 0);
        assertTrue(car.getAngle() < 0);
    }

    @Test
    public void testPairwiseWD() {
        controller.processInput(Set.of(KeyEvent.VK_W, KeyEvent.VK_D));
        assertTrue(car.getVelocity() > 0);
        assertTrue(car.getAngle() > 0);
    }

    @Test
    public void testPairwiseSA() {
        controller.processInput(Set.of(KeyEvent.VK_S, KeyEvent.VK_A));
        assertTrue(car.getVelocity() < 0);
        assertTrue(car.getAngle() < 0);
    }

    @Test
    public void testPairwiseSD() {
        controller.processInput(Set.of(KeyEvent.VK_S, KeyEvent.VK_D));
        assertTrue(car.getVelocity() < 0);
        assertTrue(car.getAngle() > 0);
    }

    /* 
    =============================================================================
    EDGE CASES 
    =============================================================================
    */

    @Test
    public void testEdgeMaxVelocity() {
        car.setVelocity(10.0);
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertEquals(10.0, car.getVelocity());
    }

    @Test
    public void testEdgeMinVelocity() {
        car.setVelocity(-5.0); 
        controller.processInput(Set.of(KeyEvent.VK_S));
        assertEquals(-5.0, car.getVelocity());

    
    @Test
    public void testEdgeJustMaxVelocity() {
        car.setVelocity(9.999); 
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertTrue(car.getVelocity() <= 10.0); 
    }

    @Test
    public void testEdgeJustMinVelocity() {
        car.setVelocity(-4.999); 
        controller.processInput(Set.of(KeyEvent.VK_S));
        assertTrue(car.getVelocity() >= -5.0); 
    }

    @Test
    public void testEdgeAboveMaxVelocity() {
        car.setVelocity(10.1); 
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertTrue(car.getVelocity() <= 10.0); 
    }

    @Test
    public void testEdgeAboveMinVelocity() {
        car.setVelocity(-5.1); 
        controller.processInput(Set.of(KeyEvent.VK_S));
        assertTrue(car.getVelocity() >= -5.0); 
    }

    
    @Test
    public void testMovementJustRightBoundary() {
        car.setX(499.9); 
        car.setVelocity(5.0);
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertTrue(car.getX() <= 500.0); 
    }

    @Test
    public void testMovementAboveRightBoundary() {
        car.setX(500.1); 
        car.setVelocity(5.0);
        controller.processInput(Set.of(KeyEvent.VK_W));
        assertTrue(car.getX() <= 500.0); 
    }

    @Test
    public void testMovementJustLeftBoundary() {
        car.setX(0.1); 
        car.setVelocity(-5.0);
        controller.processInput(Set.of(KeyEvent.VK_S));
        assertTrue(car.getX() >= 0.0);
    }

    @Test
    public void testMovementAboveLeftBoundary() {
        car.setX(-0.1); 
        car.setVelocity(-5.0);
        controller.processInput(Set.of(KeyEvent.VK_S));
        assertTrue(car.getX() >= 0.0);
    }

    /* 
      =============================================================================
        Mock Object
      =============================================================================
    */

    @Test
    public void testMockMovement() {
        
        when(mockCar.movement(anyInt())).thenReturn(true);

        mockController.processInput(Set.of(KeyEvent.VK_W));

        verify(mockCar, times(1)).movement(KeyEvent.VK_W);
        verify(mockCar, times(1)).update();
    }

    @Test
    public void testMockMovementInvalid() {
       
        when(mockCar.movement(999)).thenReturn(false);

        mockController.processInput(Set.of(999));

        verify(mockCar, times(1)).movement(999);
        verify(mockCar, times(1)).update();
    }

    @Test
    public void testMockMultipleValidKeys() {
        
        when(mockCar.movement(KeyEvent.VK_W)).thenReturn(true);
        when(mockCar.movement(KeyEvent.VK_D)).thenReturn(true);
        
        mockController.processInput(Set.of(KeyEvent.VK_W, KeyEvent.VK_D));
        
        verify(mockCar, times(1)).movement(KeyEvent.VK_W);
        verify(mockCar, times(1)).movement(KeyEvent.VK_D);
        verify(mockCar, times(1)).update();
    }

    @Test
    public void testMockMixedValidInvalidKeys() {
        
        when(mockCar.movement(KeyEvent.VK_W)).thenReturn(true);
        when(mockCar.movement(999)).thenReturn(false);

        mockController.processInput(Set.of(KeyEvent.VK_W, 999));
    
        verify(mockCar, times(1)).movement(KeyEvent.VK_W);
        verify(mockCar, times(1)).movement(999);
        verify(mockCar, times(1)).update();
    }
}
