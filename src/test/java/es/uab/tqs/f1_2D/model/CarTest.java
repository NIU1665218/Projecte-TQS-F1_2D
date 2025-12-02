package es.uab.tqs.f1_2D.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.awt.Color;
import java.awt.Graphics2D;


@ExtendWith(MockitoExtension.class)
class CarTest
{
    Car car;

    @Mock
    private BufferedImage mapImageMock;

    BufferedImage grayMap;

    //Constants
    public static final double maxVelocity = 10;
    public static final double backwardsMaxVelocity = -5;
    public static final double acceleration = 2;
    public static final double backwardsAcceleration = 0.5;
    public static final int angleMovement = 5;
    public static final int testMapHeight = 500;
    public static final int testMapWidth = 500;
    public static final int offsetSprite = 40;

    @BeforeEach
    public void setUp()
    {
        car = new Car(0,0,0f,0, maxVelocity, backwardsMaxVelocity, acceleration, backwardsAcceleration, testMapHeight, testMapWidth);
        grayMap = new BufferedImage(testMapWidth, testMapHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = grayMap.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, testMapWidth, testMapHeight);
        g2d.dispose();
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
        car.movement(Set.of(KeyEvent.VK_W));
        assertEquals(2, car.getVelocity());
        car.movement(Set.of(KeyEvent.VK_UP));
        assertEquals(4, car.getVelocity());

        //Test moviment parar/ fre amb tecles S o flecha inferior
        car.setVelocity(8);
        car.movement(Set.of(KeyEvent.VK_S));
        assertEquals(7.5, car.getVelocity());
        car.movement(Set.of(KeyEvent.VK_DOWN));
        assertEquals(7, car.getVelocity());

        //Test moviment enrere (car.getVelocity <= 0)
        car.setVelocity(0);
        car.movement(Set.of(KeyEvent.VK_S));
        assertEquals(-0.5, car.getVelocity());
        car.movement(Set.of(KeyEvent.VK_S));
        assertEquals(-1, car.getVelocity());
        car.setVelocity(0);
        car.movement(Set.of(KeyEvent.VK_DOWN));
        assertEquals(-0.5, car.getVelocity());
        car.movement(Set.of(KeyEvent.VK_DOWN));
        assertEquals(-1, car.getVelocity());

        //Test moviment lateral amb tecles A/D o fleches laterals
        car.movement(Set.of(KeyEvent.VK_A));
        assertEquals(-5, car.getAngle());
        car.movement(Set.of(KeyEvent.VK_LEFT));
        assertEquals(-10, car.getAngle());
        car.movement(Set.of(KeyEvent.VK_D));
        assertEquals(-5, car.getAngle());
        car.movement(Set.of(KeyEvent.VK_RIGHT));
        assertEquals(0, car.getAngle());

    }

    @Test
    public void trackLimitsTest()
    {
        BufferedImage map = new BufferedImage(testMapWidth, testMapHeight, BufferedImage.TYPE_INT_RGB);
        //Test que comprova si el cotxe es troba fora de pista
        map.setRGB(5 + offsetSprite, 5 + offsetSprite, Color.GREEN.getRGB());
        car.setX(5);
        car.setY(5);
        assertTrue(car.trackLimits(map));

        //Test que comprova si el cotxe es troba dins de pista
        map.setRGB(9, 9, Color.GRAY.getRGB());
        car.setX(9);
        car.setY(9);
        assertFalse(car.trackLimits(map));

        car.setX(2);
        car.setY(2);
        
        map.setRGB(2, 2, Color.RED.getRGB());
        assertFalse(car.trackLimits(map));
        
        map.setRGB(2, 2, Color.BLUE.getRGB());
        assertFalse(car.trackLimits(map));
        
        map.setRGB(2, 2, Color.BLACK.getRGB());
        assertFalse(car.trackLimits(map));
        
        map.setRGB(2, 2, Color.GRAY.getRGB());
        assertFalse(car.trackLimits(map));
        }


    @Test
    void testSingleKeyMovement() {

        boolean moved = car.movement(Set.of(KeyEvent.VK_W));
        car.update(grayMap);

        assertTrue(moved);
        assertTrue(car.getVelocity() > 0);
        assertTrue(car.getX() > 0); 
    }

    @Test
    void testMovementWD() {

        car.movement(Set.of(KeyEvent.VK_W));
        car.movement(Set.of(KeyEvent.VK_D));
        car.update(grayMap);

        assertTrue(car.getVelocity() > 0);
        assertTrue(car.getAngle() > 0); 
        assertTrue(car.getX() > 0);
        assertTrue(car.getY() >= 0); 
    }

    @Test
    void testMovementWA() {
      
        car.movement(Set.of(KeyEvent.VK_W));
        car.movement(Set.of(KeyEvent.VK_A));
        car.update(grayMap);

        assertTrue(car.getVelocity() > 0);
        assertTrue(car.getAngle() < 0);
        assertTrue(car.getX() > 0);
    }

    @Test
    void testMovementSD() {

        car.movement(Set.of(KeyEvent.VK_S));
        car.movement(Set.of(KeyEvent.VK_D));
        car.update(grayMap);

        assertTrue(car.getVelocity() < 0);
        assertTrue(car.getAngle() > 0);
    }

    @Test
    void testFrictionMovement() {
        car.setVelocity(5);
        car.update(grayMap); 
        assertTrue(car.getVelocity() < 5);
    }

    @Test
    void testBoundaryLimit() {
        car.setAngle(180);
        car.setVelocity(5);
        car.update(grayMap);
        assertTrue(car.getX() >= 0);
        assertTrue(car.getY() >= 0);
    }

    @Test
    public void testApplySlipstreamCoverage() 
    {
        //Vuelta >= 2 y está en slipstream
        car.applySlipstream(true, 2);
        car.setVelocity(maxVelocity);

        //Verifica que el boost de slipstream sea 5.0
        assertEquals(5.0, car.getEffectiveMaxVelocity() - car.getVelocity(), 0.001);
    }

    @Test
    public void testApplySlipstreamCoverage2() 
    {
        //Vuelta >= 2 pero no está en slipstream
        car.applySlipstream(false, 2);
        car.setVelocity(maxVelocity);

        //Verifica que el boost de slipstream sea 0
        assertEquals(0.0, car.getEffectiveMaxVelocity() - car.getVelocity(), 0.001);
    }

    @Test
    public void testApplySlipstream3() 
    {
        //Vuelta < 2 y está en slipstream
        car.applySlipstream(true, 1);
        car.setVelocity(maxVelocity);

        //Verifica que el boost de slipstream sea 0
        assertEquals(0.0, car.getEffectiveMaxVelocity() - car.getVelocity(), 0.001);
    }

    @Test
    public void testApplySlipstream4() {
        //Vuelta < 2 y no está en slipstream
        car.applySlipstream(false, 1);
        car.setVelocity(maxVelocity);

        //Verifica que el boost de slipstream sea 0
        assertEquals(0.0, car.getEffectiveMaxVelocity() - car.getVelocity(), 0.001);
    }

    /* 
      =============================================================================
        PAIRWISE TESTING + EDGE CASES + EQUIVALENT PARTITIONING 
      =============================================================================
    */

    /*
        VALORES A MIRAR EN PARTICIONES Y FRONTERA: (mismos que controller pero con las funciones de esta clase)
            TECLAS DE ENTRADA: W | UP + S | DOWN + A | LEFT + D | RIGHT + NO VALID 
            ESTADO DE LA VELOCIDAD (0-10, -5-0, 0)
            POSICIÓN EN EL MAPA ( X=[0, 500] y Y=[0, 500] || x/y < 0 + x/y > 500)
            SUPERFICIE EN PISTA ( EN PISTA, FUERA DE PISTA, COLISIÓN CON PARED INVISIBLE)

        - COVERAGE ACTUAL 100% DE LOS VALORES - 

        PAIRWISE TABLE:
        
        
        A	F	I	D	Combinación	   Estado Velocidad	    Ángulo	
        0	0	0	0	Ninguna	       Cero	                Cualquiera	
        0	0	0	1	Solo D	       Cero/Positiva	    Aumenta	
        0	0	1	0	Solo A	       Cero/Positiva	    Disminuye	
        0	0	1	1	A + D	       Cero/Positiva	    Neutral	
        0	1	0	0	Solo S	       Positiva/Negativa	Cualquiera	
        0	1	0	1	S + D	       Negativa	            Aumenta	
        0	1	1	0	S + A	       Negativa	            Disminuye	
        0	1	1	1	S + A + D	   Negativa	            Neutral	
        1	0	0	0	Solo W	       Cero/Positiva	    Cualquiera	
        1	0	0	1	W + D	       Positiva	            Aumenta	
        1	0	1	0	W + A	       Positiva	            Disminuye	
        1	0	1	1	W + A + D	   Positiva	            Neutral	
        1	1	0	0	W + S	       Positiva/Negativa	Cualquiera	
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
            car.movement(Set.of(KeyEvent.VK_W));
            assertEquals(i, car.getVelocity());      
        }

        car.setVelocity(backwardsMaxVelocity);
        for(double i = backwardsMaxVelocity + acceleration; i < maxVelocity; i+= acceleration)
        {
            car.movement(Set.of(KeyEvent.VK_UP));
            assertEquals(i, car.getVelocity());
        }
    }

    @Test 
    public void backwardsMovementPartitionTest()
    {
        //Test for backswards movement using S or DOWN
        car.setVelocity(maxVelocity);
        for(double i = maxVelocity - backwardsAcceleration; i > backwardsMaxVelocity; i-= backwardsAcceleration)
        {
            car.movement(Set.of(KeyEvent.VK_S));
            assertEquals(i, car.getVelocity());         
        }

        car.setVelocity(maxVelocity);
        for(double i = maxVelocity - backwardsAcceleration; i > backwardsMaxVelocity; i-= backwardsAcceleration)
        {
            car.movement(Set.of(KeyEvent.VK_DOWN));
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
            car.movement(Set.of(KeyEvent.VK_A));
            assertEquals(i, car.getAngle());         
        }

        car.setAngle(360);
        for(double i = 360 - angleMovement; i > 0; i-= angleMovement)
        {
            car.movement(Set.of(KeyEvent.VK_LEFT));
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
           car.movement(Set.of(KeyEvent.VK_D));
            assertEquals(i, car.getAngle());         
        }

        car.setAngle(0);
        for(double i = 0 + angleMovement; i < 360; i+= angleMovement)
        {
            car.movement(Set.of(KeyEvent.VK_RIGHT));
            assertEquals(i, car.getAngle());
        }
    }

    @Test 
    public void NoMovementKeyPartitionTest()
    {
        //Test for other keys not binded
        assertFalse(car.movement(Set.of(KeyEvent.VK_F)));
        assertFalse(car.movement(Set.of(KeyEvent.VK_E)));
        //...
    }

    @Test 
    public void trackLimitsPartitionTest()
    {
        BufferedImage map = new BufferedImage(testMapWidth, testMapHeight, BufferedImage.TYPE_INT_RGB);
        //Test que comprova si el cotxe es troba fora de pista
        map.setRGB(5 + offsetSprite, 5 + offsetSprite, Color.GREEN.getRGB());
        car.setX(5);
        car.setY(5);
        assertTrue(car.trackLimits(map));

        map.setRGB(4 + offsetSprite, 4 + offsetSprite, Color.BLUE.getRGB());
        car.setX(4);
        car.setY(4);
        assertTrue(car.trackLimits(map));

        map.setRGB(9 + offsetSprite, 9 + offsetSprite, Color.GRAY.getRGB());
        car.setX(9);
        car.setY(9);
        assertFalse(car.trackLimits(map));

       map.setRGB(10 + offsetSprite, 10 + offsetSprite, new Color(73, 0, 0).getRGB());
       car.setX(10);
       car.setY(10);
       assertFalse(car.trackLimits(map));

       map.setRGB(10 + offsetSprite, 10 + offsetSprite, new Color(0, 127, 0).getRGB());
       car.setX(10);
       car.setY(10);
       assertFalse(car.trackLimits(map));
       
       map.setRGB(10 + offsetSprite, 10 + offsetSprite, new Color(0, 0, 78).getRGB());
       car.setX(10);
       car.setY(10);
       assertFalse(car.trackLimits(map));
       
       map.setRGB(10 + offsetSprite, 10 + offsetSprite, new Color(73, 127, 0).getRGB());
       car.setX(10);
       car.setY(10);
       assertFalse(car.trackLimits(map));
       
       map.setRGB(10 + offsetSprite, 10 + offsetSprite, new Color(73, 0, 78).getRGB());
       car.setX(10);
       car.setY(10);
       assertFalse(car.trackLimits(map));
       
       map.setRGB(10 + offsetSprite, 10 + offsetSprite, new Color(0, 127, 78).getRGB());
       car.setX(10);
       car.setY(10);
       assertFalse(car.trackLimits(map));

       map.setRGB(10 + offsetSprite, 10 + offsetSprite, new Color(80, 130, 90).getRGB());
       car.setX(10);
       car.setY(10);
       assertTrue(car.trackLimits(map));
       
        car.setX(999);
        car.setY(999);
        assertTrue(car.trackLimits(map));

        car.setX(-1);
        car.setY(-1);
        assertTrue(car.trackLimits(map));
        
        car.setX(-1);
        car.setY(5);
        assertTrue(car.trackLimits(map));

        car.setX(5);
        car.setY(-1);
        assertTrue(car.trackLimits(map));

        car.setX(999);
        car.setY(5);
        assertTrue(car.trackLimits(map));

        car.setX(5);
        car.setY(999);
        assertTrue(car.trackLimits(map));
    }

    @Test 
    public void inviWallPartitionTest()
    {
        BufferedImage map = new BufferedImage(testMapWidth, testMapHeight, BufferedImage.TYPE_INT_RGB);
        //Test que comprova si el cotxe es troba amb una pared invisible que no permet el pas
        map.setRGB(5 + offsetSprite, 5 + offsetSprite, Color.GREEN.getRGB());
        car.setX(5);
        car.setY(5);
        assertFalse(car.invisibleWalls(map));

        map.setRGB(4 + offsetSprite, 4 + offsetSprite, Color.BLUE.getRGB());
        car.setX(4);
        car.setY(4);
        assertTrue(car.invisibleWalls(map));

        map.setRGB(9, 9, Color.GRAY.getRGB());
        car.setX(9);
        car.setY(9);
        assertFalse(car.invisibleWalls(map));

        car.setX(999);
        car.setY(999);
        assertTrue(car.invisibleWalls(map));

        car.setX(-1);
        car.setY(-1);
        assertTrue(car.invisibleWalls(map));
        
        car.setX(-1);
        car.setY(5);
        assertTrue(car.invisibleWalls(map));

        car.setX(5);
        car.setY(-1);
        assertTrue(car.invisibleWalls(map));

        car.setX(999);
        car.setY(5);
        assertTrue(car.invisibleWalls(map));

        car.setX(5);
        car.setY(999);
        assertTrue(car.invisibleWalls(map));
    }

    @Test
    public void updatePartitionTest()
    {
        //Positive Velocity
        car.setX(50);
        car.setY(50);
        car.setAngle(0);
        car.setVelocity(10);
        car.update(grayMap);
        assertTrue(car.getX() > 50);
        assertEquals(50, car.getY());

        //Negative Velocity
        car.setX(50);
        car.setY(50);
        car.setAngle(0);
        car.setVelocity(-5);
        car.update(grayMap);
        assertTrue(car.getX() < 50);
        assertEquals(50, car.getY());

        //Null Velocity
        car.setX(50);
        car.setY(50);
        car.setAngle(0);
        car.setVelocity(0.001);
        car.update(grayMap);
        assertTrue(car.getX() == 50);
        assertEquals(50, car.getY());

        //Out of bounds
        car.setX(-10);
        car.setY(testMapHeight);
        car.setAngle(0);
        car.setVelocity(-5);
        car.update(grayMap);
        assertEquals(0, car.getX());
        assertEquals(testMapHeight, car.getY());
    }

    @Test
    public void testMovementVelocityBoundaries() {
       
        car.setVelocity(maxVelocity);
        car.movement(Set.of(KeyEvent.VK_W));
        assertEquals(maxVelocity, car.getVelocity());
        
        
        car.setVelocity(backwardsMaxVelocity);
        car.movement(Set.of(KeyEvent.VK_S));
        assertEquals(backwardsMaxVelocity, car.getVelocity());
    }

    @Test
    public void testVelocitySignChange() {
        
        car.setVelocity(0.4);
        car.movement(Set.of(KeyEvent.VK_S));
        assertTrue(car.getVelocity() < 0);
        
        
        car.setVelocity(-1.0);
        car.movement(Set.of(KeyEvent.VK_W));
        assertTrue(car.getVelocity() > 0);
    }

    public void testPairwiseMovement()
    {
        int[] keys = {
            KeyEvent.VK_W, KeyEvent.VK_S, 
            KeyEvent.VK_A, KeyEvent.VK_D
        };

        double[] initialVelocities = {0, 2, -1};  
        double[] initialAngles = {0, 90, 180};    

        for (int key : keys) {
            for (double v : initialVelocities) {
                Car car1 = new Car(50, 50, 0, v, 5, -2, 0.5, 0.5, 100, 100);

                boolean moved = car.movement(Set.of(key));
                car1.update(grayMap);

                switch (key) {
                    case KeyEvent.VK_W:
                        assertTrue(moved);
                        assertTrue(car1.getVelocity() >= v);
                        break;
                    case KeyEvent.VK_S:
                        assertTrue(moved);
                        assertTrue(car1.getVelocity() <= v);
                        break;
                    case KeyEvent.VK_A:
                        assertTrue(moved);
                        assertTrue(car1.getAngle() < 0);
                        break;
                    case KeyEvent.VK_D:
                        assertTrue(moved);
                        assertTrue(car1.getAngle() > 0);
                        break;
                }
            }

            for (double angle : initialAngles) {
                Car car2 = new Car(50, 50, angle, 1, 5, -2, 0.5, 0.5, 100, 100);
                boolean moved = car2.movement(Set.of(key));
                car2.update(grayMap);

                assertTrue(moved);

                assertTrue(car2.getX() >= 0 && car2.getX() <= 100);
                assertTrue(car2.getY() >= 0 && car2.getY() <= 100);

                switch (key) {
                    case KeyEvent.VK_W:
                        assertTrue(moved);
                        switch((int)angle)
                        {
                            case 0: assertTrue(car2.getX() > 50); break;
                            case 90: assertTrue(car2.getY() > 50); break;
                            case 180: assertTrue(car2.getX() < 50); break;
                        }
                        break;
                    case KeyEvent.VK_S:
                        assertTrue(moved);
                        switch((int)angle)
                        {
                            case 0: assertTrue(car2.getX() < 50); break;
                            case 90: assertTrue(car2.getY() < 50); break;
                            case 180: assertTrue(car2.getX() > 50); break;
                        }
                        break;
                    case KeyEvent.VK_A:
                        assertTrue(moved);
                        assertTrue(car2.getAngle() < angle);
                        break;
                    case KeyEvent.VK_D:
                        assertTrue(moved);
                        assertTrue(car2.getAngle() > angle);
                        break;
                }
            }
        }
    }

    @Test
    public void testEdgeVelocityBoundary() {
       
        car.setVelocity(maxVelocity);
        car.movement(Set.of(KeyEvent.VK_W));
        assertEquals(maxVelocity, car.getVelocity());

        car.setVelocity(maxVelocity - 0.001);
        car.movement(Set.of(KeyEvent.VK_W));
        assertEquals(maxVelocity, car.getVelocity());

        car.setVelocity(maxVelocity + 0.001);
        car.movement(Set.of(KeyEvent.VK_W));
        assertEquals(maxVelocity, car.getVelocity());
        
       
        car.setVelocity(backwardsMaxVelocity);
        car.movement(Set.of(KeyEvent.VK_S));
        assertEquals(backwardsMaxVelocity, car.getVelocity());

        car.setVelocity(backwardsMaxVelocity - 0.001);
        car.movement(Set.of(KeyEvent.VK_S));
        assertEquals(backwardsMaxVelocity, car.getVelocity());

        car.setVelocity(backwardsMaxVelocity + 0.001);
        car.movement(Set.of(KeyEvent.VK_S));
        assertEquals(backwardsMaxVelocity, car.getVelocity());
    }

    @Test
    public void testEdgeMapBoundaries() {

        car.setX(0);
        car.setY(0);
        car.setAngle(225); 
        car.setVelocity(5);
        car.update(grayMap);
        assertTrue(car.getX() >= 0);
        assertTrue(car.getY() >= 0);

        car.setX(0.1);
        car.setY(0.1);
        car.setAngle(225); 
        car.setVelocity(5);
        car.update(grayMap);
        assertTrue(car.getX() >= 0);
        assertTrue(car.getY() >= 0);

        car.setX(-0.1);
        car.setY(-0.1);
        car.setAngle(225); 
        car.setVelocity(5);
        car.update(grayMap);
        assertTrue(car.getX() >= 0);
        assertTrue(car.getY() >= 0);
        
        car.setX(testMapWidth);
        car.setY(testMapHeight);
        car.setAngle(45); 
        car.setVelocity(5);
        car.update(grayMap);
        assertTrue(car.getX() <= testMapWidth);
        assertTrue(car.getY() <= testMapHeight);

        car.setX(testMapWidth - 0.1);
        car.setY(testMapHeight - 0.1);
        car.setAngle(45); 
        car.setVelocity(5);
        car.update(grayMap);
        assertTrue(car.getX() <= testMapWidth);
        assertTrue(car.getY() <= testMapHeight);

        car.setX(testMapWidth + 0.1);
        car.setY(testMapHeight + 0.1);
        car.setAngle(45); 
        car.setVelocity(5);
        car.update(grayMap);
        assertTrue(car.getX() <= testMapWidth);
        assertTrue(car.getY() <= testMapHeight);
    }

    @Test
    public void testEdgeExtremeValues() {
    
        car.setX(Double.MAX_VALUE);
        car.setY(Double.MAX_VALUE);
        car.update(grayMap);
        assertTrue(car.getX() <= testMapWidth);
        assertTrue(car.getY() <= testMapHeight);

       
        car.setX(-Double.MAX_VALUE);
        car.setY(-Double.MAX_VALUE);
        car.update(grayMap);
        assertTrue(car.getX() >= 0);
        assertTrue(car.getY() >= 0);
       
    }

    // Fricción con velocidades muy pequeñas
    @Test
    public void testEdgeFriction() {
        car.setVelocity(0.0001);
        car.update(grayMap);
        assertEquals(0.0, car.getVelocity());
        
        car.setVelocity(-0.0001);
        car.update(grayMap);
        assertEquals(0.0, car.getVelocity());
    }    

    /* 
      =============================================================================
        Mock Object
      =============================================================================
    */

    @Test
    public void testTrackLimitsFalse() {
        when(mapImageMock.getRGB(anyInt(), anyInt())).thenReturn(Color.GRAY.getRGB());

        car.setX(0);
        car.setY(0);

        assertFalse(car.trackLimits(mapImageMock));
    }

    @Test
    public void testTrackLimitsTrue() {
        when(mapImageMock.getRGB(anyInt(), anyInt())).thenReturn(Color.GREEN.getRGB());

        car.setX(0);
        car.setY(0);

        assertTrue(car.trackLimits(mapImageMock));
    }

    @Test
    public void testTrackLimitsWall()
    {
        when(mapImageMock.getRGB(anyInt(), anyInt())).thenReturn(Color.BLUE.getRGB());

        car.setX(5);
        car.setY(5);
        car.setVelocity(-0.5);
        car.update(mapImageMock);
        assertTrue(car.trackLimits(mapImageMock));
        assertTrue(5.0 == car.getX() && 5.0 == car.getY());
    }

    @Test
    public void testInviWallFalse() {
        when(mapImageMock.getRGB(anyInt(), anyInt())).thenReturn(Color.GRAY.getRGB());

        car.setX(0);
        car.setY(0);

        assertFalse(car.invisibleWalls(mapImageMock));
    }

    @Test
    public void testInviWallTrue() {
        when(mapImageMock.getRGB(anyInt(), anyInt())).thenReturn(Color.BLUE.getRGB());

        car.setX(0);
        car.setY(0);

        assertTrue(car.invisibleWalls(mapImageMock));
    }

    @Test
    public void testInviWallNoMovement()
    {
        when(mapImageMock.getRGB(anyInt(), anyInt())).thenReturn(Color.BLUE.getRGB());

        car.setX(5);
        car.setY(5);
        car.setVelocity(-0.5);
        car.update(mapImageMock);
        assertTrue(car.invisibleWalls(mapImageMock));
        assertTrue(5.0 == car.getX() && 5.0 == car.getY());
    } 
}