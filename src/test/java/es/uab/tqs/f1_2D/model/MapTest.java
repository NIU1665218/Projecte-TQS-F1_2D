package es.uab.tqs.f1_2D.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class MapTest 
{

    private Map map;
    private Map map2;
    private Rectangle finishLine;
    private List<Rectangle> checkpoints;

    @Mock
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() 
    {
        finishLine = new Rectangle(100, 100, 50, 50);
        checkpoints = new ArrayList<>();
        checkpoints.add(new Rectangle(200, 200, 30, 30));
        checkpoints.add(new Rectangle(300, 300, 30, 30));
        checkpoints.add(new Rectangle(400, 400, 30, 30));
        
        map = new Map(1000, 1000, finishLine, checkpoints);
        map.setTimeProvider(timeProvider);

        map2 = new Map(500, 500, finishLine, checkpoints);
        map2.setTimeProvider(timeProvider);
        map2.setCurrentState(Map.State.RUNNING);
        map2.setCurrentLap(2);
        map2.setBestLapTime(1500);
        map2.setLapStartTime(1000);
        map2.setLapTime(200);
        map2.setNextCheckpointIndex(1);
        map2.setCurrentLapPenalty(10);
        map2.setLapHasPenalty(true);
        map2.setRaceMode(true);
    }

    /* 
      =============================================================================
        TEST DRIVEN DEVELOPMENT (SON GETTERS Y SETTERS PRÁCTICAMENTE)
      =============================================================================
    */

    @Test
    //Verificaciones estado inicial
    void testInitialState() 
    {
        assertEquals(Map.State.IDLE, map.getState());
        assertTrue(map.getPassedCheckpoints().isEmpty());
        assertEquals(0, map.getNextCheckpointIndex());
        assertEquals(0, map.getLapTime());
        assertEquals(0, map.getBestLapTime());
    }

    @Test
    //Verificar funcionamiento reset
    public void testReset() 
    {
        map2.reset();

        assertEquals(Map.State.IDLE, map2.getState());
        assertEquals(0, map2.getCurrentLap());
        assertEquals(0, map2.getBestLapTime());
        assertEquals(0, map2.getLapTime());
        assertEquals(0, map2.getLapStartTime());
        assertTrue(map2.getPassedCheckpoints().isEmpty());
        assertEquals(0, map2.getNextCheckpointIndex());
        assertEquals(0, map2.getCurrentLapPenalty());
        assertFalse(map2.isLapHasPenalty());
        assertFalse(map2.isRaceMode());
    }

    @Test
    //Verificar reset de los sectores
    public void testResetSectors() 
    {
        map2.resetSectors();

        for (int i = 0; i < map2.getNumSectors(); i++) {
            assertEquals(0L, map2.getSectorTime(i));
            assertEquals(Map.SectorColor.NONE, map2.getSectorColor(i));
            assertFalse(map2.getSectorRecorded()[i]);
        }
    }

    @Test
    //Verificar estado inicial del modo carrera
    void testRaceInitialState() 
    {
        assertFalse(map.isRaceMode());
        assertEquals(3, map.getTotalLaps());
        assertEquals(0, map.getCurrentLap());
        assertFalse(map.isRaceComplete());
        assertFalse(map.isCountdownActive());
        assertEquals(0, map.getRemainingCountdown());
    }

    @Test
    //Test setters del modo carrera
    void testRaceSettersGetters() 
    {
        map.setRaceMode(true);
        assertTrue(map.isRaceMode());

        map.setTotalLaps(5);
        assertEquals(5, map.getTotalLaps());

        map.setCurrentLap(2);
        assertEquals(2, map.getCurrentLap());

        map.setRaceStartTime(1000L);
        assertEquals(1000L, map.getRaceStartTime());

        map.setCountdownEndTime(2000L);
        assertEquals(2000L, map.getCountdownEndTime());

        map.setCountdownSeconds(10);
        assertEquals(10, map.getCountdownSeconds());

        assertEquals(500, map2.getMapHeight());
        assertEquals(500, map2.getMapWidth());
    }

    @Test
    //Verificaciones estado inicial flag
    void testIniPassedAllCheckpoints() 
    {
        assertFalse(map.passedAllCheckpoints());
    }

    @Test
    //Verificación estado flag despues de checkpoints
    void testPassedAllCheckpoints() 
    {
       
        for (int i = 0; i < checkpoints.size(); i++) 
        {
            map.getPassedCheckpoints().add(i);
        }
        map.setNextCheckpointIndex(checkpoints.size());

        assertTrue(map.passedAllCheckpoints());
    }

    @Test
    //Test función suma sin valores
    void testSum_EmptyArray() 
    {
        long[] emptyArray = new long[0];
        assertEquals(0L, map.sum(emptyArray, 0));
    }

    @Test
    //Verificación suma de distintos tiempos de sectores
    void testSum_PartialElements() 
    {
        long[] array = {100L, 200L, 300L};
        assertEquals(100L, map.sum(array, 1)); 
        assertEquals(300L, map.sum(array, 2)); 
        assertEquals(600L, map.sum(array, 3)); 
    }

    @Test
    //Verificación coverage de fuera de indice
    void testSum_IndexOutOfBounds() 
    {
        long[] array = {100L, 200L};
        assertEquals(0L, map.sum(array, 0));
        assertEquals(100L, map.sum(array, 1));
        assertEquals(300L, map.sum(array, 10)); 
    }

    @Test
    //Verificación comportamiento correcto de los sectores
    void testSector() 
    {
        
        Rectangle finishLine = new Rectangle(100, 100, 50, 50);
        List<Rectangle> checkpoints = new ArrayList<>();
        Map map = new Map(1000, 1000, finishLine, checkpoints);
        
        
        map.setSectorTime(0, 1500L);
        map.setSectorTime(1, 2000L);
        map.setSectorTime(2, 1800L);
        
        assertEquals(1500L, map.getSectorTime(0));
        assertEquals(2000L, map.getSectorTime(1));
        assertEquals(1800L, map.getSectorTime(2));
        
        
        map.setBestSectorTime(0, 1400L);
        map.setBestSectorTime(1, 1900L);
        map.setBestSectorTime(2, 1700L);
        
        assertEquals(1400L, map.getBestSectorTime(0));
        assertEquals(1900L, map.getBestSectorTime(1));
        assertEquals(1700L, map.getBestSectorTime(2));
    }

    @Test
    //Verificación con mock el comportamiento de la progressión en una vuelta
    void testCheckpointProgression() 
    {
       
        Rectangle finishLine = new Rectangle(100, 100, 50, 50);
        List<Rectangle> checkpoints = new ArrayList<>();
        checkpoints.add(new Rectangle(200, 200, 30, 30));
        checkpoints.add(new Rectangle(300, 300, 30, 30));
        checkpoints.add(new Rectangle(400, 400, 30, 30));
        
        Map map = new Map(1000, 1000, finishLine, checkpoints);
        
        
        map.getPassedCheckpoints().add(0);
        map.setNextCheckpointIndex(1);
        assertFalse(map.passedAllCheckpoints());
        
        map.getPassedCheckpoints().add(1);
        map.setNextCheckpointIndex(2);
        assertFalse(map.passedAllCheckpoints());
        
        map.getPassedCheckpoints().add(2);
        map.setNextCheckpointIndex(3);
        assertTrue(map.passedAllCheckpoints());
    }

    @Test
    //Test getter
    void testGetBestLapTime() 
    {
        assertEquals(0L, map.getBestLapTime());

        map.setBestLapTime(1500L);
        assertEquals(1500L, map.getBestLapTime());
    }

    @Test
    //Test getter
    void testGetBestLapTimeFinish() 
    {
        assertEquals(Long.MAX_VALUE, map.getBestLapTimeFinish());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 10})
    //Test getter invalido
    void testGetSectorTimeInvalid(int invalidIndex) 
    {
        assertEquals(0L, map.getSectorTime(invalidIndex));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    //Test getter
    void testGetSectorTimeValid(int validIndex) 
    {
        map.setSectorTime(validIndex, 1000L + validIndex * 100);
        assertEquals(1000L + validIndex * 100, map.getSectorTime(validIndex));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 100})
    //Test getter invalido
    void testGetBestSectorTimeInvalid(int invalidIndex) 
    {
        assertEquals(0L, map.getBestSectorTime(invalidIndex));
    }

    @Test
    //Verificación función reset, resetea correctamente
    void resetSectors() 
    {
      
        for (int i = 0; i < 3; i++) 
        {
            map.setSectorTime(i, 1000L);
            map.setSectorColor(i, Map.SectorColor.GREEN);
            map.setSectorRecorded(i, true);
        }

        map.resetSectors();

        for (int i = 0; i < 3; i++) 
        {
            assertEquals(0L, map.getSectorTime(i));
            assertEquals(Map.SectorColor.NONE, map.getSectorColor(i));
            assertFalse(map.getSectorRecorded()[i]);
        }
    }

    @Test
    //Verificación función para el resultado de las vueltas se copia correctamente
    void copyCurrentToLastSectors() 
    {
        map.setSectorTime(0, 1000L);
        map.setSectorTime(1, 2000L);
        map.setSectorTime(2, 3000L);
        map.setSectorColor(0, Map.SectorColor.GREEN);
        map.setSectorColor(1, Map.SectorColor.ORANGE);
        map.setSectorColor(2, Map.SectorColor.PURPLE);

        map.copyCurrentToLastSectors();

        assertDoesNotThrow(() -> map.copyCurrentToLastSectors());
    }

    @Test
    //Verificar que podemos cambiar entre todos los estados
    void testStateTransitions() 
    {
        Map.State[] states = Map.State.values();
        
        for (Map.State state : states) {
            map.setCurrentState(state);
            assertEquals(state, map.getState());
        }
    }

    @Test
    //Verificar la funcionalidad para finalizar la carrera
    void testIsRaceComplete() 
    {
        map.setRaceMode(false);
        map.setCurrentLap(5);
        assertFalse(map.isRaceComplete());

        map.setRaceMode(true);
        map.setCurrentLap(2);
        map.setTotalLaps(3);
        assertFalse(map.isRaceComplete());

        map.setCurrentLap(3);
        assertFalse(map.isRaceComplete());

        map.setCurrentLap(4);
        assertTrue(map.isRaceComplete());
    }

    @Test
    //Verificar estado countdown
    void testIsCountdownActive() 
    {
        assertFalse(map.isCountdownActive());

        map.setCurrentState(Map.State.COUNTDOWN);
        assertTrue(map.isCountdownActive());

        map.setCurrentState(Map.State.RUNNING);
        assertFalse(map.isCountdownActive());
    }

    @Test
    // Test que los sectores funcionan correctamente durante una carrera
    void testSectorRace() 
    {
        map.setRaceMode(true);
        map.setCurrentLap(1);
        
        map.setSectorTime(0, 1500L);
        map.setSectorTime(1, 1200L);
        map.setSectorTime(2, 1800L);
        
        map.setSectorColor(0, Map.SectorColor.GREEN);
        map.setSectorColor(1, Map.SectorColor.PURPLE);
        map.setSectorColor(2, Map.SectorColor.ORANGE);
        
        map.setSectorRecorded(0, true);
        map.setSectorRecorded(1, true);
        map.setSectorRecorded(2, false);
        
        assertEquals(1500L, map.getSectorTime(0));
        assertEquals(1200L, map.getSectorTime(1));
        assertEquals(1800L, map.getSectorTime(2));
        
        assertEquals(Map.SectorColor.GREEN, map.getSectorColor(0));
        assertEquals(Map.SectorColor.PURPLE, map.getSectorColor(1));
        assertEquals(Map.SectorColor.ORANGE, map.getSectorColor(2));
        
        assertTrue(map.getSectorRecorded()[0]);
        assertTrue(map.getSectorRecorded()[1]);
        assertFalse(map.getSectorRecorded()[2]);
    }

    @Test
    //Verificación que termina carrera al hacer 3 vueltas
    void testFinishRace() 
    {
        map.setRaceMode(true);
        map.setCurrentLap(3);
        map.setTotalLaps(3);
    
        map.incrementLap();

        assertEquals(4, map.getCurrentLap()); 
        assertTrue(map.isRaceComplete());
        assertEquals(Map.State.RACE_FINISHED, map.getState());

    }

    /* 
      =============================================================================
        MOCK OBJECT
      =============================================================================
    */

    @Test
    //Verificación funcionalidad countdown
    void testStartCountdown() 
    {
        when(timeProvider.now()).thenReturn(1000L);

        map.startCountdown();

        assertEquals(Map.State.COUNTDOWN, map.getState());
        assertEquals(1000L + 5000, map.getCountdownEndTime()); 
        assertEquals(0, map.getCurrentLap());
        assertTrue(map.isCountdownActive());
    }

    @Test
    void testGetRemainingCountdownWhenActive() {
        long baseTime = 1000L;
        when(timeProvider.now()).thenReturn(baseTime);
        
        map.startCountdown();
        
        assertTrue(map.isCountdownActive());
        assertEquals(Map.State.COUNTDOWN, map.getState());

        when(timeProvider.now()).thenReturn(baseTime);
        int remaining = map.getRemainingCountdown();
        assertEquals(6, remaining);
        
        when(timeProvider.now()).thenReturn(baseTime + 2000L);
        remaining = map.getRemainingCountdown();
        assertEquals(4, remaining);
        
        when(timeProvider.now()).thenReturn(baseTime + 4000L);
        remaining = map.getRemainingCountdown();
        assertEquals(2, remaining);
        
        when(timeProvider.now()).thenReturn(baseTime + 4500L);
        remaining = map.getRemainingCountdown();
        assertEquals(1, remaining);
        
        when(timeProvider.now()).thenReturn(baseTime + 4999L);
        remaining = map.getRemainingCountdown();
        assertEquals(1, remaining);
    }

    @Test
    //Verificación inicialización carrera
    void testStartRace() 
    {
        when(timeProvider.now()).thenReturn(1500L);

        map.startRace();

        assertEquals(Map.State.RUNNING, map.getState());
        assertEquals(1500L, map.getRaceStartTime());
        assertEquals(1500L, map.getLapStartTime());
        assertEquals(1, map.getCurrentLap());
        
        assertTrue(map.getPassedCheckpoints().isEmpty());
        assertEquals(0, map.getNextCheckpointIndex());
        
        for (int i = 0; i < map.getNumSectors(); i++) {
            assertEquals(0L, map.getSectorTime(i));
            assertFalse(map.getSectorRecorded()[i]);
        }
    }

    @Test
    //Verificación se incrementa vuelta y se reinician sectores
    void testIncrementLap() 
    {
        map.setRaceMode(true);
        map.setCurrentLap(1);
        map.setTotalLaps(3);
        when(timeProvider.now()).thenReturn(2000L);

        map.getPassedCheckpoints().add(0);
        map.getPassedCheckpoints().add(1);
        map.setNextCheckpointIndex(2);

        map.setSectorTime(0, 1000L);
        map.setSectorTime(1, 1500L);
        map.setSectorRecorded(0, true);
        map.setSectorRecorded(1, true);

        map.incrementLap();

        assertEquals(2, map.getCurrentLap());
        assertEquals(2000L, map.getLapStartTime());

        assertTrue(map.getPassedCheckpoints().isEmpty());
        assertEquals(0, map.getNextCheckpointIndex());

        assertFalse(map.isRaceComplete());
        assertNotEquals(Map.State.RACE_FINISHED, map.getState());
    }
}