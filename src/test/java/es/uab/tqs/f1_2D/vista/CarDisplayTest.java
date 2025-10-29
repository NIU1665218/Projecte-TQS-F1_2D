package es.uab.tqs.f1_2D.vista;
import es.uab.tqs.f1_2D.model.Car;
import es.uab.tqs.f1_2D.model.Map;
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
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;

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

    /* 
      ================================================================================
        TEST SOBRE VISTA, NO ES DEMANA PERO EM SERVEIX PER VERIFICAR FUNCIONAMENT
      ================================================================================
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
        display.loadCarSprite(controller, "/img/car.png");
        assertNotNull(car.getSprite());
        assertEquals(80, car.getSprite().getWidth());
        assertEquals(80, car.getSprite().getHeight());

        //Prueba carga de sprite inválida
        assertDoesNotThrow(() -> display.loadCarSprite(controller, null));
        assertNotNull(car.getSprite());

        //Prueba carga con ruta invalida
        assertDoesNotThrow(() -> display.loadCarSprite(controller, "/img/invalid.png"));
        assertNotNull(car.getSprite());
    }

    @Test
    //Test diseñado para verificar la creación de la ventana del juego
    void testCreateGameWindow() 
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

        // Simular liberación de tecla
        display.getKeyListeners()[0].keyReleased(
            new KeyEvent(display, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W')
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
        assertDoesNotThrow(() -> {CarDisplay.main(new String[]{});});
    }

}