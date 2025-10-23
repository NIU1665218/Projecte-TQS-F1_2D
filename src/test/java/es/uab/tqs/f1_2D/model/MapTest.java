package es.uab.tqs.f1_2D.model;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.awt.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;


class MapTest {

    private Map track;
    private static final int testMapHeight = 500;
    private static final int testMapWidth = 500;
    private Rectangle finish;
    private List<Rectangle> checkpoints;

    @BeforeEach
    void setup() {

        finish = new Rectangle(100, 100, 50, 50);
        checkpoints = new ArrayList<>();
        checkpoints.add(new Rectangle(200, 100, 50, 50));
        checkpoints.add(new Rectangle(300, 150, 50, 50));
        checkpoints.add(new Rectangle(400, 200, 50, 50));
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

    /* 
      =============================================================================
        EQUIVALENT PARTITIONS
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

    /* 
    =============================================================================
    EDGE CASES 
    =============================================================================
    */

    @Test
    //Cruzar justo por el borde de la línea de meta debe contar como válido
    void testFinishLineBorder() {
        track.updatePosition(100, 125, false); 
        assertEquals(Map.State.RUNNING, track.getState());
    }

    @Test
    //Cruzar justo fuera del borde de la línea de meta no debe iniciar la vuelta
    void testFinishLineOutside() {
        track.updatePosition(99, 99, false);
        assertEquals(Map.State.IDLE, track.getState());
    }

    @Test
    //Cruzar borde de checkpoint debe marcarlo como pasado
    void testCheckpointBorder() {
        track.updatePosition(110, 110, false);
        track.updatePosition(200, 125, false); 
        assertTrue(track.getPassedCheckpoints().contains(0));
    }

    @Test
    //Cruzar justo fuera del borde del checkpoint no debe marcarlo como pasado
    void testCheckpointOutside() {
        track.updatePosition(110, 110, false);
        track.updatePosition(199, 99, false); 
        assertTrue(track.getPassedCheckpoints().isEmpty());
    }

    /* 
      =============================================================================
        Pairwise Testing
      =============================================================================
    */

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
        Rectangle mockFinish = Mockito.mock(Rectangle.class);
        when(mockFinish.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(true);
        Map mockMap = new Map(testMapWidth, testMapHeight, mockFinish, checkpoints);
        mockMap.setTimeProvider(System::currentTimeMillis);
        mockMap.updatePosition(50, 50, false);
        assertEquals(Map.State.RUNNING, mockMap.getState());
    }

    @Test
    //Mock de checkpoint detectado
    void testCheckpointMock() {
        Rectangle mockFinish = Mockito.mock(Rectangle.class);
        Rectangle mockCheckpoint = Mockito.mock(Rectangle.class);
        when(mockFinish.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(false);
        when(mockCheckpoint.contains(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(true);

        List<Rectangle> mockCheckpoints = List.of(mockCheckpoint);
        Map track = new Map(testMapWidth, testMapHeight, mockFinish, mockCheckpoints);

        track.updatePosition(150, 150, false);

        assertTrue(track.getPassedCheckpoints().contains(0));
    }

    @Test
    //Mock del tiempo
    void testLapTimeMock() {
        TimeProvider mockTime = Mockito.mock(TimeProvider.class);
        when(mockTime.now()).thenReturn(1000L, 2000L); 

        Map track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.setTimeProvider(mockTime);

        track.updatePosition(110, 110, false);
        track.updatePosition(210, 110, false);
        track.updatePosition(310, 160, false);
        track.updatePosition(110, 110, false);

        assertEquals(Map.State.RESULT, track.getState());
        assertEquals(1000, track.getLapTime());
    }

    @Test
    //Mock de la conexión con el coche
    void testCarMock() {
        Car mockCar = Mockito.mock(Car.class);
        when(mockCar.getX()).thenReturn(110.0);
        when(mockCar.getY()).thenReturn(110.0);

        Map track = new Map(testMapWidth, testMapHeight, finish, checkpoints);
        track.updatePosition(mockCar.getX(), mockCar.getY(), false);

        assertEquals(Map.State.RUNNING, track.getState());
    }


}
