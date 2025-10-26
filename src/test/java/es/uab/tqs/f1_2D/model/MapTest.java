package es.uab.tqs.f1_2D.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class MapTest 
{

    private Map map;
    private Rectangle finishLine;
    private List<Rectangle> checkpoints;

    @BeforeEach
    void setUp() 
    {
        finishLine = new Rectangle(100, 100, 50, 50);
        checkpoints = new ArrayList<>();
        checkpoints.add(new Rectangle(200, 200, 30, 30));
        checkpoints.add(new Rectangle(300, 300, 30, 30));
        checkpoints.add(new Rectangle(400, 400, 30, 30));
        
        map = new Map(1000, 1000, finishLine, checkpoints);
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
    //Verificación con mock el comportamiento correcto de los sectores
    void testSectorOperations() 
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
    // Verificar que podemos cambiar entre todos los estados
    void testStateTransitions() 
    {
        Map.State[] states = Map.State.values();
        
        for (Map.State state : states) {
            map.setCurrentState(state);
            assertEquals(state, map.getState());
        }
    }
}