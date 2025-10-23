package es.uab.tqs.f1_2D.model;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


class MapTest {

    private Map track;
    private static final int testMapHeight = 500;
    private static final int testMapWidth = 500;
    private Rectangle finish;
    private List<Rectangle> checkpoints;
    private static final int offsetSprite = 40;

    @BeforeEach
    void setup() {

        finish = new Rectangle(100, 100, 80, 80);
        checkpoints = new ArrayList<>();
        checkpoints.add(new Rectangle(200, 100, 80, 80));
        checkpoints.add(new Rectangle(300, 150, 80, 80));
        checkpoints.add(new Rectangle(400, 200, 80, 80));
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(System::currentTimeMillis);
    }

    /* 
      =============================================================================
        TEST DRIVEN DEVELOPMENT 
      =============================================================================
    */

    @Test
    //Al cruzar la línea de meta por primera vez se inicia la vuelta
    void testStartLap() {
        track.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Al cruzar la línea de meta por primera vez se inicia la vuelta
    void testCheckpoint() {
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false); 
        assertTrue(track.getPassedCheckpoints().contains(0));
    }

    @Test
    //Al cruzar la línea de meta tras pasar todos los checkpoints se completa la vuelta
    void testLapCompletion() {
        track.updatePosition(110, 110, false); 
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(410, 210, false);
        track.updatePosition(110, 110, false);
        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //No se completa la vuelta si no se han pasado todos los checkpoints
    void testLapNotCompleted() {
        track.updatePosition(110, 110, false);
        track.updatePosition(110, 110, false);
        assertNotEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //Salir de la pista detiene la vuelta
    void testOffTrackStopsLap() {
        track.updatePosition(110, 110, false);
        track.updatePosition(120, 120, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
        assertEquals(0, track.getLapTime());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    @Test
    //Resetear el mapa vuelve al estado inicial
    void testResetFunctionality() {
        track.updatePosition(110, 110, false);
        track.updatePosition(120, 120, false);
        track.reset();
        assertEquals(Map.State.IDLE, track.getState());
        assertEquals(0, track.getLapTime());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    @Test
    //Al completar una vuelta se guarda el mejor tiempo
    void testBestLapTime() throws InterruptedException {
        track.updatePosition(110, 110, false);
        Thread.sleep(10);
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(110, 110, false);
        long firstLap = track.getLapTime();

        track.reset();
        track.updatePosition(110, 110, false);
        Thread.sleep(5);
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(110, 110, false);

        assertTrue(track.getLapTime() < firstLap);
    }

    @Test
    //Al completar vuelta transiciona de RESULT a RUNNING
    void testResultToRunningTransition() throws InterruptedException 
    {
        track.updatePosition(110, 110, false); 
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(410, 210, false);
        track.updatePosition(110, 110, false);
        assertEquals(Map.State.RESULT, track.getState());

        Thread.sleep(3500);
        assertEquals(Map.State.RUNNING, track.getState());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    /* 
      =============================================================================
        PAIRWISE TESTING + EDGE CASES + EQUIVALENT PARTITIONING 
      =============================================================================
    */

    @Test
    //Estado IDLE → RUNNING al cruzar línea de meta sin estar fuera de pista
    void testIdleToRunningTransition() {
        track.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Estado RUNNING → COMPLETED solo si todos los checkpoints están pasados
    void testRunningToCompletedTransition() {
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(110, 110, false);
        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //Estado RUNNING → OFF_TRACK si offTrack es true
    void testRunningToOffTrackTransition() {
        track.updatePosition(110, 110, false);
        track.updatePosition(120, 120, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }

    @Test
    //Estado offTrack = true, cualquier posición 
    void testOffTrackAnyPosition() {
        
        track.updatePosition(50, 50, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
     
        track.updatePosition(110, 110, false); 
        track.updatePosition(120, 120, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
        
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(410, 210, false);
        track.updatePosition(110, 110, false); 
        track.updatePosition(130, 130, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }

    @Test
    //Estado offTrack = false, todos checkpoints pasados
    void testNoOffTrackFinishLineAllCheckpoints() {
    
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(410, 210, false);
        track.updatePosition(110, 110, false);
        
        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    //Estado offTrack = false, posición checkpoints
    void testNoOffTrackCheckpointPosition() {
        
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false);
        
        assertTrue(track.getPassedCheckpoints().contains(0));
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Estado offTrack = false, posición regular
    void testRegularPosition() {
            
        track.updatePosition(110, 110, false);
        track.updatePosition(150, 150, false);
        
        assertEquals(Map.State.RUNNING, track.getState());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    @Test
    //Estado offTrack, posición NO en meta
    void tesOffTrackNotFinishLine() {
        
        track.updatePosition(50, 50, true); 
        track.updatePosition(80, 80, false); 
        
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }
   
    @Test
    // Frontera interna de la línea de meta
    void testFinishLineBoundaryInside() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);

        // Esquina superior izquierda
        track.updatePosition(100 - offsetSprite, 100 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);
        // Esquina superior derecha
        track.updatePosition(179 - offsetSprite, 100 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);
        // Esquina inferior izquierda
        track.updatePosition(100 - offsetSprite, 179 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);
        // Esquina inferior derecha
        track.updatePosition(179 - offsetSprite, 179 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);
        // Centro
        track.updatePosition(125 - offsetSprite, 125 - offsetSprite, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Frontera externa de la línea de meta
    void testFinishLineBoundaryOutside() {
       
        // Esquina superior izquierda
        track.updatePosition(99 - offsetSprite, 99 - offsetSprite, false);
        assertEquals(Map.State.IDLE, track.getState());
        
        // Justo fuera - superior derecha
        track.updatePosition(180 - offsetSprite, 99 - offsetSprite, false);
        assertEquals(Map.State.IDLE, track.getState());
        
        // Justo fuera - inferior izquierda
        track.updatePosition(99 - offsetSprite, 180 - offsetSprite, false);
        assertEquals(Map.State.IDLE, track.getState());
        
        // Justo fuera - inferior derecha
        track.updatePosition(180 - offsetSprite, 180 - offsetSprite, false);
        assertEquals(Map.State.IDLE, track.getState());
    }

    @Test
    void testCheckpointBoundaryInside() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        track.updatePosition(110, 110, false); 
    
        // Esquina superior izquierda
        track.updatePosition(200 - offsetSprite, 100 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(0));
        track.updatePosition(300 - offsetSprite, 150 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(1));
        track.updatePosition(400 - offsetSprite, 200 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(2));

        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);
        track.updatePosition(110, 110, false);
        // Esquina superior derecha
        track.updatePosition(279 - offsetSprite, 100 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(0));
        track.updatePosition(379 - offsetSprite, 150 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(1));
        track.updatePosition(479 - offsetSprite, 200 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(2));
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);
        track.updatePosition(110, 110, false);
        // Esquina inferior izquierda
        track.updatePosition(200 - offsetSprite, 179 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(0));
        track.updatePosition(300 - offsetSprite, 229 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(1));
        track.updatePosition(400 - offsetSprite, 279 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(2));
        
        track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);
        track.updatePosition(110, 110, false);
        // Esquina inferior derecha
        track.updatePosition(279 - offsetSprite, 179 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(0));
        track.updatePosition(379 - offsetSprite, 229 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(1));
        track.updatePosition(479 - offsetSprite, 279 - offsetSprite, false);
        assertTrue(track.getPassedCheckpoints().contains(2));

    }

    @Test
    void testCheckpointBoundaryOutside() {
        track.updatePosition(110, 110, false); // Inicia vuelta
        
        //Esquina superior izquierda
        track.updatePosition(199 - offsetSprite, 99 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        track.updatePosition(299 - offsetSprite, 149 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        track.updatePosition(399 - offsetSprite, 199  - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        
        //Esquina superior derecha
        track.updatePosition(280 - offsetSprite, 99 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        track.updatePosition(380 - offsetSprite, 149 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        track.updatePosition(480 - offsetSprite, 199 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        
        //Esquina inferior izquierda
        track.updatePosition(199 - offsetSprite, 180 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        track.updatePosition(299 - offsetSprite, 230 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        track.updatePosition(399 - offsetSprite, 280 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        
        //Esquina inferior derecha
        track.updatePosition(280 - offsetSprite, 150 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        track.updatePosition(380 - offsetSprite, 230 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
        track.updatePosition(480 - offsetSprite, 280 - offsetSprite, false);
        assertFalse(track.getPassedCheckpoints().contains(0));
    }

    @Test
    //En meta + offTrack 
    void testFinishLineOffTrackPairwise() {
        track.updatePosition(110, 110, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }

    @Test
    // IDLE + En meta + sin offTrack = Empieza vuelta
    void testIdleStartsLap() {
        track.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    // RUNNING + En meta + todos checkpoints = Termina vuelta
    void testRunningAtFinishLineCompletesLap() {
        track.updatePosition(110, 110, false); 
        track.updatePosition(210, 110, false); 
        track.updatePosition(310, 160, false); 
        track.updatePosition(110, 110, false); 
        assertEquals(Map.State.RESULT, track.getState());
    }

    @Test
    // IDLE + fuera de meta + offTrack = OFF_TRACK 
    void testIdleOffTrackPairwise() {
        track.updatePosition(400, 400, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
    }

    @Test
    //Checkpoint + offTrack = OFF_TRACK sin marcar checkpoint
    void testCheckpointOffTrackPairwise() {
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    @Test
    //OFF_TRACK + FinishLine = Empieza vuelta
    void testOffTrackStartsPairwise() {
        track.updatePosition(110, 110, false);
        track.updatePosition(120, 120, true);
        assertEquals(Map.State.OFF_TRACK, track.getState());
        track.updatePosition(110, 110, false);
        assertEquals(Map.State.RUNNING, track.getState());
    }

    /* 
      =============================================================================
        Mock Object
      =============================================================================
    */

    @Test
    //Verificar detección de línea de meta
    void testUpdatePositionMock() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        Rectangle mockFinish = Mockito.mock(Rectangle.class);
        when(mockFinish.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(true);
        Map mockMap = new Map(testMapWidth, testMapHeight, mockFinish, checkpoints);
        mockMap.setTimeProvider(mockTime);
        mockMap.updatePosition(50, 50, false);
        assertEquals(Map.State.RUNNING, mockMap.getState());
    }

    @Test
    //Mock de checkpoint detectado
    void testCheckpointMock() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        Rectangle mockFinish = Mockito.mock(Rectangle.class);
        Rectangle mockCheckpoint = Mockito.mock(Rectangle.class);
        when(mockFinish.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(false);
        when(mockCheckpoint.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(true);

        List<Rectangle> mockCheckpoints = List.of(mockCheckpoint);
        Map track = new Map(testMapWidth, testMapHeight, mockFinish, mockCheckpoints);

        track.setTimeProvider(mockTime);
        track.updatePosition(150, 150, false);

        assertTrue(track.getPassedCheckpoints().contains(0));
    }

    @Test
    //Mock del tiempo
    void testLapTimeMock() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 3000L);
        track.setTimeProvider(mockTime);
        
        track.updatePosition(110, 110, false); 
        track.updatePosition(210, 110, false); 
        track.updatePosition(310, 160, false);   
        track.updatePosition(410, 210, false); 
        track.updatePosition(110, 110, false); 
        
        assertEquals(2000L, track.getLapTime());
        assertEquals(500L, track.getSectorTime(0));
        assertEquals(500L, track.getSectorTime(1));
        assertEquals(500L, track.getSectorTime(2));
        
        verify(mockTime, times(5)).now();
    }

    @Test
    //Segunda vuelta mejora el mejor tiempo
    void testBestLapTimeUpdated() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L, 3000L, 5000L, 7000L, 9000L);
        track.setTimeProvider(mockTime);
        
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(410, 210, false);
        track.updatePosition(110, 110, false);
        
        long firstLapTime = track.getLapTime();
        
        when(mockTime.now()).thenReturn(10000L, 11000L, 12000L, 13000L, 14000L);
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(410, 210, false);
        track.updatePosition(110, 110, false);
        
        assertTrue(track.getBestLapTime() < firstLapTime);
    }

    @Test
    //Tiempos de sectores tras completar vuelta
    void testResultStateShowsSectorTimes() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L, 1500L, 2000L, 2500L, 3000L);
        track.setTimeProvider(mockTime);
        
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false); 
        track.updatePosition(310, 160, false); 
        track.updatePosition(410, 210, false); 
        track.updatePosition(110, 110, false);
        
        assertTrue(track.getSectorTime(0) > 0);
        assertTrue(track.getSectorTime(1) > 0);
        assertTrue(track.getSectorTime(2) > 0);
    }

    @Test
    void testFinishLineMock() {
        // Mock de Rectangle para línea de meta
        Rectangle mockFinish = Mockito.mock(Rectangle.class);
        when(mockFinish.contains(anyDouble(), anyDouble())).thenReturn(true);
        
        Map trackWithMockFinish = new Map(testMapWidth, testMapHeight, mockFinish, checkpoints);
        trackWithMockFinish.updatePosition(50, 50, false);
        
        assertEquals(Map.State.RUNNING, trackWithMockFinish.getState());
        verify(mockFinish).contains(50 + 40, 50 + 40); // Verificar offset del coche
    }

    @Test
    // Mock colores de sectores
    void testSectorColorsWithMockTime() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        
        when(mockTime.now()).thenReturn(1000L, 1200L, 1500L, 1900L, 2400L);
        track.setTimeProvider(mockTime);
        
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false); // Sector 1: 200
        track.updatePosition(310, 160, false); // Sector 2: 300
        track.updatePosition(410, 210, false); // Sector 3: 400
        track.updatePosition(110, 110, false);
        
        
        when(mockTime.now()).thenReturn(3000L, 3200L, 3450L, 3900L, 4400L);
        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false); // Sector 1: 200
        track.updatePosition(310, 160, false); // Sector 2: 250 
        track.updatePosition(410, 210, false); // Sector 3: 450 
        track.updatePosition(110, 110, false);
        
        // Verificar colores de sectores
        assertEquals(Map.SectorColor.ORANGE, track.getSectorColor(0)); 
        assertEquals(Map.SectorColor.GREEN, track.getSectorColor(1));  
        assertEquals(Map.SectorColor.ORANGE, track.getSectorColor(2)); 
    }

    @Test
    //Mock de la conexión con el coche
    void testCarMock() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L);
        Car mockCar = Mockito.mock(Car.class);
        when(mockCar.getX()).thenReturn(110.0);
        when(mockCar.getY()).thenReturn(110.0);

        Map track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);
        track.updatePosition(mockCar.getX(), mockCar.getY(), false);

        assertEquals(Map.State.RUNNING, track.getState());
    }


}
