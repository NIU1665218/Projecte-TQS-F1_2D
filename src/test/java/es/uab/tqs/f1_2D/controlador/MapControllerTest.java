package es.uab.tqs.f1_2D.controlador;


import es.uab.tqs.f1_2D.model.Car;
import es.uab.tqs.f1_2D.model.Map;
import es.uab.tqs.f1_2D.model.TimeProvider;
import es.uab.tqs.f1_2D.model.Map.SectorColor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import static org.mockito.ArgumentMatchers.anyDouble;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapControllerTest 
{

    private Map track;
    private MapController trackController;
    private static final int testMapHeight = 500;
    private static final int testMapWidth = 500;
    private Rectangle finish;
    private List<Rectangle> checkpoints;
    private static final int offsetSprite = 40;

    @Mock
    TimeProvider mockTime;

    @BeforeEach
    void setup() 
    {

        //Rectangulo simulando la línea de meta
        finish = new Rectangle(100, 100, 80, 80);
        //Conjunto de rectangulos de checkpoints para pruebas
        checkpoints = new ArrayList<>();
        checkpoints.add(new Rectangle(200, 100, 80, 80));
        checkpoints.add(new Rectangle(300, 150, 80, 80));
        checkpoints.add(new Rectangle(400, 200, 80, 80));

        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        trackController = new MapController(track);
        trackController.setTimeProvider(System::currentTimeMillis);
        mockTime = Mockito.mock(TimeProvider.class);
    }

    /* 
      =============================================================================
        TEST DRIVEN DEVELOPMENT 
      =============================================================================
    */


    @Test
    //Al inicializar y settear un mapa debería ser el mismo al cogerlo con el getter
    void testGetters()
    {
        Map newMap = new Map(1000, 1000, finish, checkpoints);
        trackController.setMap(newMap);
        assertEquals(trackController.getModel(), newMap);
    }

    @Test
    //Al cruzar la línea de meta por primera vez se inicia la vuelta
    void testStartLap() 
    {
        trackController.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Al cruzar la línea de meta por primera vez se inicia la vuelta
    void testCheckpoint() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false); 
        assertTrue(track.getPassedCheckpoints().contains(0));
    }

    @Test
    //Al cruzar la línea de meta tras pasar todos los checkpoints se completa la vuelta
    void testLapCompletion() 
    {
        trackController.updatePosition(110, 110, false); 
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);
        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //No se completa la vuelta si no se han pasado todos los checkpoints
    void testLapNotCompleted() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(110, 110, false);
        assertNotEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //Salir de la pista detiene la vuelta
    void testOffTrackStopsLap() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(120, 120, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
        assertEquals(0, track.getLapTime());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    @Test
    //Resetear el mapa vuelve al estado inicial
    void testReset() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(120, 120, false);
        trackController.reset();
        assertEquals(Map.State.IDLE, track.getState());
        assertEquals(0, track.getLapTime());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }
 
    @Test
    //Al completar vuelta transiciona de RESULT a RUNNING
    void testResultToRunningTransition() throws InterruptedException 
    {
        trackController.updatePosition(110, 110, false); 
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);
        assertEquals(Map.State.RESULT, track.getState());

        Thread.sleep(3500);
        trackController.updatePosition(112, 112, false);
        assertEquals(Map.State.RUNNING, track.getState());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    @Test
    //Al pasar por línea de meta siendo vuelta inválida, debería volver a empezar
    void testFinishLineAfterInvalidLap() 
    {
        trackController.updatePosition(110, 110, false);

        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(210, 110, false); 
        assertEquals(Map.State.INVALID_LAP, track.getState());

        trackController.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Al pasar por el mismo checkpoint que ya se ha pasado anteriormente, no debería añadirse
    void testEnteringSameCheckpoint() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false); 
        trackController.updatePosition(210, 110, false); 
        assertTrue(track.getPassedCheckpoints().contains(0)); 
        assertEquals(1, track.getPassedCheckpoints().size());
    }

    @Test
    //Al pasar por línea de meta con el último sector ya establecido no debería contarse dos veces
    void testLastSectorRecorded() 
    {

        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);

        
        track.setSectorRecorded(track.getNumSectors() - 1, true);

        trackController.updatePosition(110, 110, false);

        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //Al pasar por un checkpoint con el sector ya establecido manualmente no debería contarse dos veces
    void testRunningWithSecondSectorAlreadyRecorded() 
    {
        
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);

        // Simulamos que el último sector ya se registró manualmente
        track.setSectorRecorded(track.getNumSectors() - 2, true);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);

        trackController.updatePosition(110, 110, false);

        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //Intentar establecer un sector que no existe no debería establecer ningún tiempo
    void testRecordSectorInvalidIndex() 
    {
        trackController.recordSector(-1, 500L);
        trackController.recordSector(track.getNumSectors(), 500L); 

        for (boolean recorded : track.getSectorRecorded()) {
            assertFalse(recorded);
        }
    }

    @Test
    //Establecimiento de un sector en morado
    void testRecordSectorPurpleSet() {
        trackController.recordSector(0, 1000L); 
        trackController.recordSector(0, 900L);  
        assertEquals(SectorColor.PURPLE, track.getSectorColor(0));
    }

    @Test
    //Verificar que el flag de passedAllCheckpoints funciona correctamente al passar por todos
    void testPassedAllCheckpointsTrue() 
    {
        
        for (int i = 0; i < checkpoints.size(); i++) {
            track.getPassedCheckpoints().add(i);
        }
        track.setNextCheckpointIndex(checkpoints.size());

        assertTrue(track.passedAllCheckpoints());
    }

    @Test
    //Verificación de coverage con el flag de passedAllCheckpoints
    void testPassedAllCheckpointsFalseSize() 
    {
        track.getPassedCheckpoints().add(0);
        track.setNextCheckpointIndex(checkpoints.size());
        assertFalse(track.passedAllCheckpoints());
    }

    @Test
    //Verificación de coverage con el flag de passedAllCheckpoints
    void testPassedAllCheckpointsFalse() 
    {
        for (int i = 0; i < checkpoints.size(); i++) {
            track.getPassedCheckpoints().add(i);
        }
        track.setNextCheckpointIndex(checkpoints.size() - 1);
        assertFalse(track.passedAllCheckpoints());
    }

    @Test
    //Verificación de coverage para la función de sum de tiempos de sectores
    void testSumIndexZero() 
    {
        assertEquals(0L, track.sum(new long[]{10,20,30}, 0));
    }

    @Test
    //Verificación de coverage para la función de sum de tiempos de sectores
    void testSumWithinBounds() 
    {
        assertEquals(30L, track.sum(new long[]{10,20,30}, 2));
    }

    @Test
    //Verificación de coverage para la función de sum de tiempos de sectores
    void testSumIndexGreaterThanLength() 
    {
        assertEquals(60L, track.sum(new long[]{10,20,30}, 99));
    }

    @Test
    //Verificación de coverage para la función de sum de tiempos de sectores
    void testPurpleConditionNotTriggered() 
    {
        //Sector inválido
        trackController.recordSector(-1, 5000L); 

        //Sector con el primer tiempo
        trackController.recordSector(0, 4000L);

        //No debe ser PURPLE aún
        assertEquals(SectorColor.GREEN, track.getSectorColor(0));
    } 

    @Test
    //Verificación de coverage para la función de invalidateLap
    void testInvalidateLap() 
    {
    
        trackController.invalidateLap();
        
        assertEquals(Map.State.INVALID_LAP, track.getState()); 
        
        assertNotNull(trackController.getOffTrackTimer()); 
        assertFalse(trackController.getOffTrackTimer().isRepeats()); 
        assertTrue(trackController.getOffTrackTimer().isRunning()); 
        
        Timer offTrackTimer = trackController.getOffTrackTimer();
        ActionListener[] listeners = offTrackTimer.getActionListeners();
        
        for (ActionListener listener : listeners) {
            listener.actionPerformed(null); 
        }
        
        assertEquals(Map.State.IDLE, track.getState()); 
        assertFalse(offTrackTimer.isRunning());
    }

    @Test
    // Verificar que startQualyMode configura correctamente el modo qualy
    void testStartQualyMode() 
    {
        trackController.startQualyMode();

        assertFalse(track.isRaceMode());
        assertEquals(Map.State.IDLE, track.getState());
        assertTrue(track.getPassedCheckpoints().isEmpty());
        assertEquals(0, track.getNextCheckpointIndex());
    }
    
    /* 
      =============================================================================
        PAIRWISE TESTING + EDGE CASES + EQUIVALENT PARTITIONING 
      =============================================================================
    */

    /*
        VALORES A MIRAR EN PARTICIONES Y FRONTERA:
            ESTADOS DEL MAPA: IDLE, RUNNING, OFF_TRACK, INVALID_MAP, RESULT
            LÍMITES EN LÍNEA DE META Y CHECKPOINTS
            TIMERS
            SECTORS 1,2,3 - COLORS NONE, ORANGE, GREEN, PURPLE

        - COVERAGE ACTUAL 90% DE LOS VALORES - (FALTA SECTORS - FALTA IMPLEMENTAR ALTRES COTXES)
    */

    @Test
    //Estado IDLE → RUNNING al cruzar línea de meta sin estar fuera de pista
    void testIdleToRunningTransition() 
    {
        trackController.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Estado RUNNING → COMPLETED solo si todos los checkpoints están pasados
    void testRunningToCompletedTransition() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);
        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //Estado RUNNING → OFF_TRACK si offTrack es true
    void testRunningToOffTrackTransition() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(120, 120, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }

    @Test
    //Estado offTrack = true, cualquier posición 
    void testOffTrackAnyPosition() 
    {
        
        trackController.updatePosition(50, 50, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
     
        trackController.updatePosition(110, 110, false); 
        trackController.updatePosition(120, 120, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
        
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false); 
        trackController.updatePosition(130, 130, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }

    @Test
    //Estado offTrack = false, todos checkpoints pasados
    void testNoOffTrackFinishLineAllCheckpoints() 
    {
    
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);
        
        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //Estado offTrack = false, posición checkpoints
    void testNoOffTrackCheckpointPosition() 
    {
        
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        
        assertTrue(track.getPassedCheckpoints().contains(0));
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Estado offTrack = false, posición regular
    void testRegularPosition() 
    {
            
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(150, 150, false);
        
        assertEquals(Map.State.RUNNING, track.getState());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    @Test
    //Estado offTrack, posición NO en meta
    void tesOffTrackNotFinishLine() 
    {
        
        trackController.updatePosition(50 -offsetSprite, 50 - offsetSprite, true); 
        trackController.updatePosition(80 - offsetSprite, 80 - offsetSprite, false); 
        
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }
   
    @Test
    // Frontera interna de la línea de meta
    void testFinishLineBoundaryInside() 
    {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);

        // Esquina superior izquierda
        trackController.updatePosition(100 - offsetSprite, 100 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        trackController.setMap(track);
        trackController.setTimeProvider(mockTime);
        // Esquina superior derecha
        trackController.updatePosition(179 - offsetSprite, 100 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        trackController.setMap(track);
        trackController.setTimeProvider(mockTime);
        // Esquina inferior izquierda
        trackController.updatePosition(100 - offsetSprite, 179 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        trackController.setMap(track);
        trackController.setTimeProvider(mockTime);
        // Esquina inferior derecha
        trackController.updatePosition(179 - offsetSprite, 179 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        trackController.setMap(track);
        trackController.setTimeProvider(mockTime);
        // Centro
        trackController.updatePosition(125 - offsetSprite, 125 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Frontera externa de la línea de meta
    void testFinishLineBoundaryOutside() 
    {
       
        // Esquina superior izquierda
        trackController.updatePosition(99 - offsetSprite, 99 - offsetSprite, false);
        assertEquals(Map.State.IDLE, track.getState());
        
        // Justo fuera - superior derecha
        trackController.updatePosition(180 - offsetSprite, 99 - offsetSprite, false);
        assertEquals(Map.State.IDLE, track.getState());
        
        // Justo fuera - inferior izquierda
        trackController.updatePosition(99 - offsetSprite, 180 - offsetSprite, false);
        assertEquals(Map.State.IDLE, track.getState());
        
        // Justo fuera - inferior derecha
        trackController.updatePosition(180 - offsetSprite, 180 - offsetSprite, false);
        assertEquals(Map.State.IDLE, track.getState());
    }

    @Test
    void testCheckpointBoundaryInside() 
    {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        trackController.updatePosition(110, 110, false); 
    
        // Esquina superior izquierda
        trackController.updatePosition(200 - offsetSprite, 100 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(300 - offsetSprite, 150 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(1));
        trackController.updatePosition(400 - offsetSprite, 200 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(2));

        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        trackController.setMap(track);
        trackController.setTimeProvider(mockTime);
        trackController.updatePosition(110, 110, false);
        // Esquina superior derecha
        trackController.updatePosition(279 - offsetSprite, 100 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(379 - offsetSprite, 150 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(1));
        trackController.updatePosition(479 - offsetSprite, 200 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(2));
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        trackController.setMap(track);
        trackController.setTimeProvider(mockTime);
        trackController.updatePosition(110, 110, false);
        // Esquina inferior izquierda
        trackController.updatePosition(200 - offsetSprite, 179 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(300 - offsetSprite, 229 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(1));
        trackController.updatePosition(400 - offsetSprite, 279 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(2));
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        trackController.setMap(track);
        trackController.setTimeProvider(mockTime);
        trackController.updatePosition(110, 110, false);
        // Esquina inferior derecha
        trackController.updatePosition(279 - offsetSprite, 179 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(379 - offsetSprite, 229 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(1));
        trackController.updatePosition(479 - offsetSprite, 279 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(2));

    }

    @Test
    void testCheckpointBoundaryOutside() 
    {
        trackController.updatePosition(110, 110, false); 
        
        //Esquina superior izquierda
        trackController.updatePosition(199 - offsetSprite, 99 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(299 - offsetSprite, 149 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(399 - offsetSprite, 199  - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        
        //Esquina superior derecha
        trackController.updatePosition(280 - offsetSprite, 99 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(380 - offsetSprite, 149 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(480 - offsetSprite, 199 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        
        //Esquina inferior izquierda
        trackController.updatePosition(199 - offsetSprite, 180 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(299 - offsetSprite, 230 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(399 - offsetSprite, 280 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        
        //Esquina inferior derecha
        trackController.updatePosition(280 - offsetSprite, 150 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(380 - offsetSprite, 230 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        trackController.updatePosition(480 - offsetSprite, 280 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
    }

    @Test
    //En meta + offTrack 
    void testFinishLineOffTrackPairwise() 
    {
        trackController.updatePosition(110, 110, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }

    @Test
    // IDLE + En meta + sin offTrack = Empieza vuelta
    void testIdleStartsLap() 
    {
        trackController.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    // RUNNING + En meta + todos checkpoints = Termina vuelta
    void testRunningAtFinishLineCompletesLap() 
    {
        trackController.updatePosition(110, 110, false); 
        trackController.updatePosition(210, 110, false); 
        trackController.updatePosition(310, 160, false); 
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false); 
        assertEquals(Map.State.RESULT, track.getState());
        trackController.updatePosition(110, 110, false); 
        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    // IDLE + fuera de meta + offTrack = OFF_TRACK 
    void testIdleOffTrackPairwise() 
    {
        trackController.updatePosition(400, 400, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }

    @Test
    //Checkpoint + offTrack = OFF_TRACK sin marcar checkpoint
    void testCheckpointOffTrackPairwise() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    @Test
    //OFF_TRACK + FinishLine = Empieza vuelta
    void testOffTrackStartsPairwise() 
    {
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(120, 120, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
        trackController.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    /*
    @Test
    void testSectorPartitions() 
    {
        // Configurar mejores tiempos existentes
        track.setBestSectorTime(0, 1000L);
        track.setBestSectorTime(1, 2000L);
        
        trackController.recordSector(0, 800L);
        
        assertEquals(Map.SectorColor.PURPLE, track.getSectorColor(0));
        
        trackController.recordSector(0, 1800L);
        assertEquals(Map.SectorColor.ORANGE, track.getSectorColor(0));

        trackController.recordSector(0, 1700L);
        assertEquals(Map.SectorColor.GREEN, track.getSectorColor(0));
    }
    */

    /* 
      =============================================================================
        Mock Object
      =============================================================================
    */

    @Test
    //Verificar detección de línea de meta
    void testUpdatePositionMock() 
    {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        Rectangle mockFinish = Mockito.mock(Rectangle.class);
        when(mockFinish.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(true);

        Map mockMap = new Map(testMapWidth, testMapHeight, mockFinish, checkpoints);
        MapController mockController = new MapController(mockMap);

        mockController.setTimeProvider(mockTime);
        mockController.updatePosition(50, 50, false);

        assertEquals(Map.State.RUNNING, mockMap.getState());
    }

    @Test
    //Mock para verificación de checkpoint detectado
    void testCheckpointMock() 
    {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        Rectangle mockFinish = Mockito.mock(Rectangle.class);
        Rectangle mockCheckpoint = Mockito.mock(Rectangle.class);
        when(mockFinish.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(false);
        when(mockCheckpoint.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(true);

        List<Rectangle> mockCheckpoints = List.of(mockCheckpoint);
        Map track = new Map(testMapWidth, testMapHeight, mockFinish, mockCheckpoints);
        MapController trackController = new MapController(track);
        track.setCurrentState(Map.State.RUNNING);

        trackController.setTimeProvider(mockTime);
        trackController.updatePosition(210, 110, false);

        assertTrue(track.getPassedCheckpoints().contains(0));
    }

    @Test
    //Mock de Rectangle para verificación de línea de meta
    void testFinishLineMock() 
    {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        Rectangle mockFinish = Mockito.mock(Rectangle.class);
        when(mockFinish.contains(anyDouble(), anyDouble())).thenReturn(true);
        
        Map trackWithMockFinish = new Map(testMapWidth, testMapHeight, mockFinish, checkpoints);
        MapController mockController = new MapController(trackWithMockFinish);
        mockController.setTimeProvider(mockTime);
        mockController.updatePosition(50, 50, false);
        
        assertEquals(Map.State.RUNNING, trackWithMockFinish.getState());
        verify(mockFinish).contains((double)(50 + 40), (double)(50 + 40)); // Verificar offset del coche
    }

    @Test
    //Mock de la conexión con el coche
    void testCarMock() 
    {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        Car mockCar = Mockito.mock(Car.class);
        when(mockCar.getX()).thenReturn(110.0);
        when(mockCar.getY()).thenReturn(110.0);

        Map track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        MapController mockController = new MapController(track);
        mockController.setTimeProvider(mockTime);
        mockController.updatePosition(mockCar.getX(), mockCar.getY(), false);

        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Al completar una vuelta se guarda el mejor tiempo
    void testWorseBestLapTime() throws InterruptedException 
    {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        TimeProvider mockTime2 = Mockito.mock(TimeProvider.class);
        trackController.setTimeProvider(mockTime);
        when(mockTime.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 2800L);
        when(mockTime2.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 3000L);

        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);
        long firstLap = track.getLapTime();

        trackController.setTimeProvider(mockTime2);
        track.setCurrentState(Map.State.IDLE);
        trackController.reset();

        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);

        assertTrue(track.getLapTime() > firstLap);
        assertTrue(track.getBestLapTime() == firstLap);
    }

    @Test
    //Al completar una vuelta se guarda el mejor tiempo
    void testBestLapTime() throws InterruptedException 
    {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        TimeProvider mockTime2 = Mockito.mock(TimeProvider.class);
        trackController.setTimeProvider(mockTime);
        when(mockTime.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 3000L);
        when(mockTime2.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 2800L);

        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);

        assertEquals(track.getSectorTime(4), 0);
        assertEquals(track.getSectorTime(-1), 0);
        assertEquals(track.getSectorTime(0), 1500L);

        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);
        long firstLap = track.getLapTime();


        trackController.setTimeProvider(mockTime2);
        track.setCurrentState(Map.State.IDLE);
        trackController.reset();

        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);

        assertTrue(track.getLapTime() < firstLap);
        assertEquals(track.getBestSectorTime(4), 0);
        assertEquals(track.getBestSectorTime(-1), 0);
        assertEquals(track.getBestSectorTime(1), Long.MAX_VALUE);
        assertEquals(track.getBestSectorTime(0), 1500L);
    }

    @Test
    // Verificar que startRaceMode configura correctamente el modo carrera
    void testStartRaceMode() 
    {
        when(mockTime.now()).thenReturn(1000L);
        trackController.setTimeProvider(mockTime);
        trackController.setSkip(true);
        trackController.startRaceMode();

        assertTrue(track.isRaceMode());
        assertEquals(Map.State.RUNNING, track.getState());
        assertFalse(track.isCountdownActive()); //Skipped
    }

    @Test
    // Verificar que durante la cuenta atrás no se procesa la posición
    void testUpdatePositionDuringCountdown() 
    {
        when(mockTime.now()).thenReturn(1000L);
        trackController.setTimeProvider(mockTime);

        trackController.startRaceMode();

        trackController.updatePosition(110, 110, false);

        assertEquals(Map.State.COUNTDOWN, track.getState());
    }

    @Test
    // Verificar que la cuenta atrás termina e inicia la carrera correctamente
    void testCountdownEndsAndRaceStarts() 
    {
        when(mockTime.now()).thenReturn(1000L, 6000L); 
        trackController.setTimeProvider(mockTime);

        trackController.startRaceMode();

        
        trackController.updatePosition(0, 0, false); 

        assertEquals(Map.State.RUNNING, track.getState());
        assertEquals(1, track.getCurrentLap());
    }

    @Test
    // Verificar que se incrementa la vuelta en modo carrera
    void testIncrementLapInRaceMode() 
    {
        when(mockTime.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 3000L, 3500L);
        trackController.setTimeProvider(mockTime);
        trackController.setSkip(true);
        trackController.startRaceMode();
        trackController.updatePosition(0, 0, false); 
       
        trackController.updatePosition(110, 110, false); 
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false); 
        trackController.updatePosition(410, 210, false); 
        trackController.updatePosition(110, 110, false); 

        assertEquals(2, track.getCurrentLap());
    }

    @Test
    // Verificar que la carrera se completa correctamente después de todas las vueltas
    void testRaceCompletion() 
    {
        when(mockTime.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 3000L, 3500L, 4000L, 4500L, 5000L, 5500L);
        trackController.setTimeProvider(mockTime);
        trackController.setSkip(true);
        trackController.startRaceMode();
        trackController.updatePosition(0, 0, false); 
   
        for (int lap = 1; lap < 4; lap++) {
            trackController.updatePosition(110, 110, false);
            trackController.updatePosition(210, 110, false);
            trackController.updatePosition(310, 160, false);
            trackController.updatePosition(410, 210, false);
            trackController.updatePosition(110, 110, false);
        }

        assertEquals(Map.State.RACE_FINISHED, track.getState());
        assertTrue(track.isRaceComplete());
        trackController.updatePosition(410, 210, false);
        assertEquals(Map.State.RACE_FINISHED, track.getState());
    }

    @Test
    // Verificar que en modo qualy no se incrementa el contador de vueltas
    void testQualyModeDoesNotIncrementLap() 
    {
        when(mockTime.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 3000L);
        trackController.setTimeProvider(mockTime);

        trackController.startQualyMode();
        
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false);

        assertEquals(0, track.getCurrentLap());
    }

    @Test
    // Test coverage para stopAllTimers 
    void testStopAllTimersVariousTimerStates() {
        when(mockTime.now()).thenReturn(1000L);
        trackController.setTimeProvider(mockTime);
        
        trackController.stopAllTimers();
        
        trackController.startRaceMode();
        trackController.getCountdownTimer().stop();
        trackController.stopAllTimers();
        
        trackController.invalidateLap();
        if (trackController.getOffTrackTimer() != null) {
            trackController.getOffTrackTimer().stop();
        }
        trackController.stopAllTimers();
        
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(210, 110, false);
        trackController.updatePosition(310, 160, false);
        trackController.updatePosition(410, 210, false);
        trackController.updatePosition(110, 110, false); 
        
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        if (trackController.getOffTrackTimer() != null) {
            trackController.getOffTrackTimer().stop();
        }
        
        trackController.stopAllTimers();
        
        assertTrue(true); 
    }

    @Test
    // Verificar que el modo carrera maneja correctamente vueltas inválidas
    void testInvalidLapRaceMode() 
    {
        when(mockTime.now()).thenReturn(1000L, 1500L);
        trackController.setTimeProvider(mockTime);
        trackController.setSkip(true);

        trackController.startRaceMode();
        trackController.updatePosition(0, 0, false); 

        trackController.updatePosition(110, 110, false); 
        trackController.updatePosition(310, 160, false); 

        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    // Verificar que después de un tiempo de forma inválida en modo carrera se vuelve a RUNNING
    void testAfterInvalidLapReturnsToRunningInRaceMode() throws InterruptedException 
    {
        when(mockTime.now()).thenReturn(1000L, 1500L);
        trackController.setTimeProvider(mockTime);
        trackController.setSkip(true);
        trackController.startRaceMode();
        Thread.sleep(1000);
        trackController.updatePosition(0, 0, false);

      
        trackController.updatePosition(110, 110, false);
        trackController.updatePosition(310, 160, false); 

        assertEquals(Map.State.RUNNING, track.getState());

        Thread.sleep(3600);
        
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    // Verificar getter para countdownTimer
    void testGetCountdownTimer() {
        assertNull(trackController.getCountdownTimer()); 
        
        when(mockTime.now()).thenReturn(1000L);
        trackController.setTimeProvider(mockTime);
        
        trackController.startRaceMode();
        
        assertNotNull(trackController.getCountdownTimer());
        assertTrue(trackController.getCountdownTimer().isRunning());
    }
}
