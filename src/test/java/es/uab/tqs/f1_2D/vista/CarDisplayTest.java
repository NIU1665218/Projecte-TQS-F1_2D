package es.uab.tqs.f1_2D.vista;
import es.uab.tqs.f1_2D.model.AICar;
import es.uab.tqs.f1_2D.model.Car;
import es.uab.tqs.f1_2D.model.Map;
import es.uab.tqs.f1_2D.vista.CarDisplay.GameMode;
import es.uab.tqs.f1_2D.controlador.AIController;
import es.uab.tqs.f1_2D.controlador.CarController;
import es.uab.tqs.f1_2D.controlador.MapController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.lang.reflect.Field;
import java.util.List;

import javax.sound.sampled.Clip;

import javax.swing.JFrame;

@ExtendWith(MockitoExtension.class)
class CarDisplayTest {

    @Mock
    private CarController mockController;
    private CarController realController;
    @Mock
    private BufferedImage mockMap;
    private BufferedImage realMap;
    @Mock
    private BufferedImage mockCollisionMap;
    private BufferedImage realCollisionMap;
    @Mock
    private Car mockCar;
    private Car realCar;
    @Mock
    private Clip mockClip;
    @Mock
    private Map mockMap2;
    @Mock
    private LapUI lapUI;
    @Mock
    private MapController mockMapController;
    @Mock
    private AIController mockAiController;

    /* 
      ============================================================================================
        TEST SOBRE VISTA, NO ES DEMANA PERO EM SERVEIX PER VERIFICAR FUNCIONAMENT I FER COVERAGE
      ============================================================================================
    */

    @BeforeEach
    void setUp()
    {
        // Configuración común antes de cada test si es necesario
        mockClip = mock(Clip.class);
        mockController = mock(CarController.class);
        mockMap = mock(BufferedImage.class);
        mockCollisionMap = mock(BufferedImage.class);
        lapUI = new LapUI(mockMap2);
        mockAiController = mock(AIController.class);
    }

    @Test
    //Test diseñado para verificar la carga de imágenes en CarDisplay
    void testLoadImage() throws IOException 
    {

        //Prueba carga de imagen válida 
        BufferedImage result = CarDisplay.loadImage("/track/bahrain.jpg", 2.0);
        assertNotNull(result);
        assertEquals(2560, result.getWidth());
        assertEquals(1440, result.getHeight());

        //Prueba carga de imagen inválida
        assertDoesNotThrow(() -> 
        CarDisplay.loadImage(null, 2.0));
    }

    @Test 
    //Test diseñado para verificar que se carga el sprite del coche
    void testLoadCarSprite() throws IOException 
    {
        Car car = new Car(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);
        CarController controller = new CarController(car);
        CarDisplay display = new CarDisplay(controller, null, null);

        //Prueba carga de sprite válida 
        display.loadCarSprite(car, "/img/car.png");
        assertNotNull(car.getSprite());
        assertEquals(80, car.getSprite().getWidth());
        assertEquals(80, car.getSprite().getHeight());

        //Prueba carga de sprite inválida
        assertDoesNotThrow(() -> display.loadCarSprite(car, null));
        assertNotNull(car.getSprite());

        //Prueba carga con ruta invalida
        assertDoesNotThrow(() -> display.loadCarSprite(car, "/img/invalid.png"));
        assertNotNull(car.getSprite());
    }

    @Test
    void testStartGameRaceMode() 
    {
        Car car = new Car(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);
        when(mockController.getCar()).thenReturn(car);
        when(mockMap2.getFinishLine()).thenReturn(new Rectangle(0, 0, 100, 10));
        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);

        setPrivateField(display, "trackMap", mockMap2);
        setPrivateField(display, "aiController", mockAiController);
        setPrivateField(display, "trackMapController", mockMapController);

        display.startGame(GameMode.RACE);
    
        assertFalse(display.isInMainMenu());
        assertEquals(GameMode.RACE, display.getCurrentGameMode());
    
        assertEquals(350, car.getX());
        assertEquals(1250, car.getY());
        assertEquals(280, car.getAngle());
        assertEquals(0, car.getVelocity());
    
        assertNotNull(getPrivateField(display, "aiController"));
    
        verify(mockMap2).setTotalLaps(3);
        verify(mockMapController).startRaceMode();
    }

    @Test
    void testStartGameQualyMode()
    {
        Car car = new Car(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);
        when(mockController.getCar()).thenReturn(car);

        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);

        setPrivateField(display, "trackMap", mockMap);
        setPrivateField(display, "trackMapController", mockMapController);

        display.startGame(GameMode.QUALY);

        assertFalse(display.isInMainMenu());
        assertEquals(GameMode.QUALY, display.getCurrentGameMode());
        assertEquals(880, car.getX());
        assertEquals(1780, car.getY());
        assertEquals(267, car.getAngle());
        assertEquals(0, car.getVelocity());
        assertNull(getPrivateField(display, "aiController"));

        verify(mockMapController).startQualyMode();
    }

    @Test
    void testIsRaceFinished_PlayerFinished() 
    {
        when(mockMap2.isRaceComplete()).thenReturn(true);
        Car car = new Car(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);
        when(mockController.getCar()).thenReturn(car);
    
        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);
        display.setIsMainMenu(false);
        setPrivateField(display, "currentGameMode", GameMode.RACE);
        setPrivateField(display, "trackMap", mockMap2);
        setPrivateField(display, "aiController", mockAiController);
    
        if(GraphicsEnvironment.isHeadless())
        {
            assertThrows(HeadlessException.class, () -> {display.updateRace();});
        }
        else
        {
            assertDoesNotThrow(() -> display.updateRace());
        }
    }
    
    @Test
    void testIsRaceFinished_AIFinished() 
    {
        when(mockMap2.isRaceComplete()).thenReturn(false);
        Car car = new Car(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);
        when(mockController.getCar()).thenReturn(car);
        AICar mockAICar = mock(AICar.class);
        when(mockAICar.getCurrentLap()).thenReturn(4);
        when(mockMap2.getTotalLaps()).thenReturn(3);
        when(mockAiController.getAICars()).thenReturn(List.of(mockAICar));
        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);
        display.setIsMainMenu(false);
        setPrivateField(display, "aiController", mockAiController);
        setPrivateField(display, "trackMap", mockMap2);
        setPrivateField(display, "currentGameMode", GameMode.RACE);
    
        if(GraphicsEnvironment.isHeadless())
        {
            assertThrows(HeadlessException.class, () -> {display.updateRace();});
        }
        else
        {
            assertDoesNotThrow(() -> display.updateRace());
        }
    }
    
    @Test
    void testIsRaceFinished_NoOneFinished() 
    {
        Car car = new Car(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);
        when(mockController.getCar()).thenReturn(car);

        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);
        setPrivateField(display, "aiController", mockAiController);
        setPrivateField(display, "trackMap", mockMap2);
        setPrivateField(display, "currentGameMode", GameMode.RACE);
    
        assertDoesNotThrow(() -> display.updateRace());
    }

    @Test
    //Test diseñado para verificar la creación de la ventana del juego
    void testCreateGameWindow() 
    {
        if(!GraphicsEnvironment.isHeadless())
        {
            //Creación de los componentes necesarios para el test
            Car car = new Car(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);
            CarController controller = new CarController(car);
            BufferedImage dummy = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

            JFrame frame = CarDisplay.createGame(controller, dummy, dummy);

            //Verificar que la ventana se ha creado correctamente
            assertEquals("F1 2D", frame.getTitle());
            assertTrue(frame.getContentPane().getComponent(0) instanceof CarDisplay);
        }
    }

    @Test
    //La imagen del coche no se pueden cargar
    void testCarDisplayNoRecursos() 
    {
        
        //Configuración del mock
        when(mockController.getCar()).thenReturn(mockCar);
        
        // Crear CarDisplay con recursos null para forzar excepciones y no debería lanzar excepción
        assertDoesNotThrow(() -> new CarDisplay(mockController, null, null));     
    }

    @Test
    //No produce excepción pintar un sprite nulo del coche
    void testCarDisplayPaintComponentNoSprite() 
    {


        //Configuración del mock
        when(mockController.getCar()).thenReturn(mockCar);
            
        //Creación del resto de componentes necesarios para la creación del display
        BufferedImage map = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        CarDisplay display = new CarDisplay(mockController, map, map);
        display.setBounds(0, 0, 500, 500);

        Graphics2D g2d = (Graphics2D) new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB).getGraphics();

        //Asegurar que no lanza ninguna excepción
        assertDoesNotThrow(() -> 
        display.paintComponent(g2d));
    }

    @Test
    void testPaintComponentWithSprite() 
    {
        //Configuración del mock
        when(mockController.getCar()).thenReturn(mockCar);
       
        //Creación del resto de componentes necesarios para la creación del display
        BufferedImage map = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        CarDisplay display = new CarDisplay(mockController, map, map);
        display.setBounds(0, 0, 500, 500);

        //Asegurar que no lanza ninguna excepción
        Graphics2D g2d = (Graphics2D) new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB).getGraphics();
        assertDoesNotThrow(() -> display.paintComponent(g2d));
    }


    @Test
    void testKeyListenerAddsAndRemovesKeys() throws Exception
    {
        //Configuración del mock
        Map mockTempMap = mock(Map.class);
        when(mockTempMap.isCountdownActive()).thenReturn(false);
    
        realCar = new Car(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);
        realController = new CarController(realCar);
        CarDisplay display = new CarDisplay(realController, realMap, realCollisionMap);
        display.setIsMainMenu(false);

        //Inyectar mockTempMap para poder devolver false a isCountDownActive y poder realizar el test
        Field f = CarDisplay.class.getDeclaredField("trackMap");
        f.setAccessible(true);
        f.set(display, mockTempMap);

        // Simular pulsación de tecla
        display.getKeyListeners()[0].keyPressed(
            new KeyEvent(display, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W')
        );
        assertTrue(display.getKeysPressed().contains(KeyEvent.VK_W));

        display.setIsMainMenu(true);

        display.getKeyListeners()[0].keyReleased(
            new KeyEvent(display, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W')
        );
        assertTrue(display.getKeysPressed().contains(KeyEvent.VK_W));

        display.setIsMainMenu(false);
        when(mockTempMap.isCountdownActive()).thenReturn(true);
        display.getKeyListeners()[0].keyReleased(
            new KeyEvent(display, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W')
        );
        assertTrue(display.getKeysPressed().contains(KeyEvent.VK_W));


        when(mockTempMap.isCountdownActive()).thenReturn(false);
        // Simular liberación de tecla
        display.getKeyListeners()[0].keyReleased(
            new KeyEvent(display, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W')
        );
        assertFalse(display.getKeysPressed().contains(KeyEvent.VK_W));

        // Simular pulsación de tecla
        display.getKeyListeners()[0].keyPressed(
            new KeyEvent(display, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, 'W')
        );
        assertTrue(display.isInMainMenu());

        display.setIsMainMenu(false);
        when(mockTempMap.isCountdownActive()).thenReturn(true);
        display.getKeyListeners()[0].keyPressed(
            new KeyEvent(display, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W')
        );
        assertFalse(display.getKeysPressed().contains(KeyEvent.VK_W));

        display.setIsMainMenu(true);
        display.getKeyListeners()[0].keyPressed(
            new KeyEvent(display, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W')
        );
        assertFalse(display.getKeysPressed().contains(KeyEvent.VK_W));

    }

    @Test
    void testUpdateRaceOffTrackState() 
    {
        //Configuración del mock
        when(mockController.getCar()).thenReturn(mockCar);

        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);
        display.getOffTrackOverlay().setClip(mockClip);
        display.getInvalidLapOverlay().setClip(mockClip);

        // Configurar el estado del mapa a OFF_TRACK
        display.getTrackMap().setCurrentState(Map.State.OFF_TRACK);
        //Asegurar que no lanza ninguna excepción
        assertDoesNotThrow(() -> display.updateRace());
    }

    @Test
    void testUpdateRaceInvalidLapState() 
    {
        //Configuración del mock
        when(mockController.getCar()).thenReturn(mockCar);
 
        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);
        display.getOffTrackOverlay().setClip(mockClip);
        display.getInvalidLapOverlay().setClip(mockClip);

        // Configurar el estado del mapa a INVALID_LAP
        display.getTrackMap().setCurrentState(Map.State.INVALID_LAP);
        //Asegurar que no lanza ninguna excepción
        assertDoesNotThrow(() -> display.updateRace());
    }

    @Test
    void testUpdateRaceNormalState() 
    {
        //Configuración del mock
        when(mockController.getCar()).thenReturn(mockCar);
        
        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);
        display.getOffTrackOverlay().setClip(mockClip);
        display.getInvalidLapOverlay().setClip(mockClip);

        // Configurar el estado del mapa a RUNNING
        display.getTrackMap().setCurrentState(Map.State.RUNNING);
        //Asegurar que no lanza ninguna excepción
        assertDoesNotThrow(() -> display.updateRace());
    }

    @Test
    void testUpdateRaceResultState() 
    {
        //Configuración del mock
        when(mockController.getCar()).thenReturn(mockCar);

        CarDisplay display = new CarDisplay(mockController, mockMap, mockCollisionMap);
        display.getOffTrackOverlay().setClip(mockClip);
        display.getInvalidLapOverlay().setClip(mockClip);

        // Configurar el estado del mapa a RESULT
        display.getTrackMap().setCurrentState(Map.State.RESULT);
        //Asegurar que no lanza ninguna excepción
        assertDoesNotThrow(() -> display.updateRace());
    }

    @Test
    //Test para asegurar que el método main no lanza excepciones (No sé si es necesario)
    void testMainDoesNotThrow() 
    {
        if(GraphicsEnvironment.isHeadless())
        {
            assertThrows(HeadlessException.class,() -> {CarDisplay.main(new String[]{});});
        }
        else
        {
            assertDoesNotThrow(() -> {CarDisplay.main(new String[]{});});
        }
    }

    //Métodos de ayuda para poder hacer una especie de caballo de troya
    private void setPrivateField(Object obj, String fieldName, Object value) 
    {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            return;
        }
    }

    private Object getPrivateField(Object obj, String fieldName) 
    {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }


}