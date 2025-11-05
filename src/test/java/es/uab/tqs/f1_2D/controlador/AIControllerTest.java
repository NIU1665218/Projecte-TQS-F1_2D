package es.uab.tqs.f1_2D.controlador;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uab.tqs.f1_2D.model.*;

public class AIControllerTest 
{
    private Map mockMap;
    private Map map;
    private Car mockPlayerCar;
    private RandomGenerator mockRandom;
    private AIController controller;
    private AIController controller2;
    private AICar mockAI;
    private TimeProvider mocktimeProvider;
    @BeforeEach
    public void setUp()
    {
        mockMap = mock(Map.class);
        mockPlayerCar = mock(Car.class);
        mockRandom = mock(RandomGenerator.class);

        Rectangle finishLine = new Rectangle(100, 100, 50, 50);
        List<Rectangle> checkpoints = new ArrayList<>();
        checkpoints.add(new Rectangle(200, 200, 70, 70));
        checkpoints.add(new Rectangle(300, 300, 70, 70));
        checkpoints.add(new Rectangle(400, 400, 70, 70));
        
        map = new Map(1000, 1000, finishLine, checkpoints);

        controller = new AIController(mockMap, mockPlayerCar, mockRandom);
        controller2 = new AIController(map, mockPlayerCar);
        mockAI = mock(AICar.class);
        mocktimeProvider = mock(TimeProvider.class);
        controller.setTimeProvider(mocktimeProvider);

        long[] bestTimes = controller.getBestGlobalSectorTimes();
        Arrays.fill(bestTimes, Long.MAX_VALUE);

    }

    /* 
      =============================================================================
        TEST DRIVEN DEVELOPMENT
      =============================================================================
    */

    @Test
   //Inicializar todos los componentes
    void testConstructor() 
    {
        assertEquals(19, controller.getAICars().size());
        assertTrue(controller.getRacingLine().size() > 50); 
        assertEquals(Long.MAX_VALUE, controller.getBestGlobalSectorTimes()[0]);
    }

    @Test
    //La skill level no debe ser la misma para todos los coches
    void testSetAIDifficulty()
     {
        double antes = controller.getAICars().get(0).getSkillLevel();
        controller.setAIDifficulty(0.5);
        double despues = controller.getAICars().get(0).getSkillLevel();
        assertTrue(despues <= antes);
        assertTrue(despues > 0);
    }

    @Test
    //Reset debería reiniciar estado de todo
    void testReset() 
    {
        controller.reset();
        assertEquals(19, controller.getAICars().size());
        assertArrayEquals(new long[]{Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE},
                controller.getBestGlobalSectorTimes());
    }

    @Test
    //Verificar la posición de un coche
    void testGetAIPosition() 
    {
        List<AICar> aiCars = controller.getAICars();
        AICar first = aiCars.get(0);
        assertEquals(1, controller.getAIPosition(first));
    }

    @Test
    //Comprobar el cálculo de ángulo hacia el primer punto
    void testAngleCalculationInRange() throws Exception 
    {
        double angle = controller.calculateAngleToWaypoint(400, 1200, 1);
        assertTrue(angle >= 0 && angle <= 360);
    }

    @Test
    //Comprobar que se cálcula bien a pesar de no tener la racing line bien definida
    void testCalculateAngleToWaypoint() 
    {
        controller2.getRacingLine().clear();
        double angleDefault = controller2.calculateAngleToWaypoint(0, 0, 1);
        assertEquals(280, angleDefault);

        ArrayList<Point> line = new ArrayList<>();
        line.add(new Point(0, 0));
        line.add(new Point(10, 0));
        controller2.getRacingLine().addAll(line);

        double anglePositive = controller2.calculateAngleToWaypoint(0, 0, 1);
        assertEquals(0, anglePositive);
    }

    /* 
      =============================================================================
        BLACK BOX
      =============================================================================
    */

    
    @Test
    //Verificar el funcionamiento de las posiciones
    void testUpdatePositions() 
    {
        List<AICar> cars = controller.getAICars();
        AICar ai1 = cars.get(0);
        AICar ai2 = cars.get(1);

        ai1.setCurrentLap(2);
        ai2.setCurrentLap(1);

        List<AICar> positions = controller.getPositions();
        assertTrue(positions.contains(ai1));
        assertEquals(1, controller.getAIPosition(ai1));
    }

    /* 
      =============================================================================
        Mock Object
      =============================================================================
    */

    @Test
    //Verificar el funcionamiento de UpdateAllAI para que se puedan mover y hacer tiempos
    void testUpdateAllAIWithMocksWithCheckpoint() 
    {
        AICar mockAI = mock(AICar.class);
        when(mockAI.getX()).thenReturn(200.0);
        when(mockAI.getY()).thenReturn(200.0);
        when(mockAI.getPassedCheckpoints()).thenReturn(new HashSet<>());
        when(mockAI.getSectorTimes()).thenReturn(new long[]{0, 0, 0});
        when(mockAI.getNextCheckpointIndex()).thenReturn(0);
        when(mockAI.getSectorColors()).thenReturn(new Map.SectorColor[]{Map.SectorColor.NONE, Map.SectorColor.NONE, Map.SectorColor.NONE});

        List<AICar> aiCars = new ArrayList<>();
        aiCars.add(mockAI);

        // inject mock list
        var aiField = getPrivateField(controller2, "aiCars");
        setPrivateFieldValue(controller2, aiField, aiCars);

        BufferedImage mockImg = mock(BufferedImage.class);
        controller2.updateAllAI(mockImg);

        verify(mockAI, atLeastOnce()).updateAI(any(), anyList(), anyInt());
        verify(mockAI, atLeastOnce()).updateLapTime();
    }

    @Test
    //Verificar el funcionamiento de UpdateAllAI para que se puedan mover y hacer tiempos
    void testUpdateAllAIWithMocksWithOut() 
    {
        AICar mockAI = mock(AICar.class);
        when(mockAI.getX()).thenReturn(0.0);
        when(mockAI.getY()).thenReturn(0.0);
        when(mockAI.getPassedCheckpoints()).thenReturn(new HashSet<>());
        when(mockAI.getSectorTimes()).thenReturn(new long[]{0, 0, 0});
        when(mockAI.getNextCheckpointIndex()).thenReturn(0);
        when(mockAI.getSectorColors()).thenReturn(new Map.SectorColor[]{Map.SectorColor.NONE, Map.SectorColor.NONE, Map.SectorColor.NONE});

        List<AICar> aiCars = new ArrayList<>();
        aiCars.add(mockAI);

        // inject mock list
        var aiField = getPrivateField(controller2, "aiCars");
        setPrivateFieldValue(controller2, aiField, aiCars);

        BufferedImage mockImg = mock(BufferedImage.class);
        controller2.updateAllAI(mockImg);

        verify(mockAI, atLeastOnce()).updateAI(any(), anyList(), anyInt());
        verify(mockAI, atLeastOnce()).updateLapTime();
    }

    @Test
    //Verificar el funcionamiento de finish line LapStartTime == 0 -> solo startNewLap()
    void testHandleFinishLineStartLap() 
    {
        
        AICar mockAI = mock(AICar.class);
        when(mockAI.getLapStartTime()).thenReturn(0L);
        when(mockAI.passedAllCheckpoints()).thenReturn(false);

        controller.handleAIFinishLine(mockAI);
     
        verify(mockAI, never()).completeLap();
        verify(mockAI).startNewLap();
    }

    @Test
    //Verificar el funcionamiento de finish line LapStartTime > 0 y passedAllCheckpoints() == true -> completeLap() y startNewLap()
    void testHandleFinishLineAllCheckpointsPassed() throws Exception 
    {
        when(mockAI.getLapStartTime()).thenReturn(5000L);
        when(mockAI.passedAllCheckpoints()).thenReturn(true);

        controller.handleAIFinishLine(mockAI);

        verify(mockAI).completeLap();
        verify(mockAI).startNewLap();
    }

    @Test
    //Verificar el funcionamiento de finish line LapStartTime > 0 y passedAllCheckpoints() == false -> no hace nada
    void testHandleAIFinishLine_NotPassedAllCheckpoints() throws Exception 
    {
        when(mockAI.getLapStartTime()).thenReturn(5000L);
        when(mockAI.passedAllCheckpoints()).thenReturn(false);

        controller.handleAIFinishLine(mockAI);

        verify(mockAI, never()).completeLap();
        verify(mockAI, never()).startNewLap();
    }

    @Test
    //Comprueba que los coches AI tambien están sujetos a FinishLine y checkpoints
    void testHandleAIFinishLineCalled() throws Exception 
    {
        when(mockAI.getX()).thenReturn(100.0);
        when(mockAI.getY()).thenReturn(100.0);
        when(mockAI.getLapStartTime()).thenReturn(0L);
        when(mockAI.passedAllCheckpoints()).thenReturn(true);

        Rectangle finishLine = new Rectangle(0, 0, 500, 500);
        when(mockMap.getFinishLine()).thenReturn(finishLine);

        var method = AIController.class.getDeclaredMethod("updateAICheckpoints", AICar.class);
        method.setAccessible(true);
        method.invoke(controller, mockAI);
        
        verify(mockAI, atLeastOnce()).startNewLap();
    }

    @Test
    //Verificar el funcionamiento del sort cogiendo ambos valores
    void testUpdatePositionsPlayerIA() throws Exception
    {
        when(mockMap.getCurrentLap()).thenReturn(3);

        when(mockAI.getCurrentLap()).thenReturn(2);
        when(mockAI.getPassedCheckpoints()).thenReturn(new HashSet<>());
        when(mockAI.getX()).thenReturn(150.0);
        when(mockAI.getY()).thenReturn(150.0);

        when(mockPlayerCar.getX()).thenReturn(100.0);
        when(mockPlayerCar.getY()).thenReturn(100.0);

        Car dummyCar = mock(Car.class);
        when(dummyCar.getX()).thenReturn(200.0);
        when(dummyCar.getY()).thenReturn(200.0);

        List<AICar> aiCars = List.of(mockAI);
        setPrivateFieldValue(controller, getPrivateField(controller, "aiCars"), aiCars);

        List<Car> extraCars = new ArrayList<>();
        extraCars.add(mockAI);
        extraCars.add(dummyCar);
        setPrivateFieldValue(controller, getPrivateField(controller, "aiCars"), extraCars);

        controller.updatePositions();

        verify(mockMap, atLeastOnce()).getCurrentLap();
    }

    @Test
    void testCheckpointAlreadyPassed() 
    {
        Set<Integer> passed = new HashSet<>(List.of(2));
        when(mockAI.getPassedCheckpoints()).thenReturn(passed);

        controller.handleAICheckpoint(mockAI, 2);

        verify(mockAI, never()).recordSector(anyInt(), anyLong());
    }

    @Test
    void testCheckpointRecordSector() 
    {
        when(mockAI.getPassedCheckpoints()).thenReturn(new HashSet<>());
        when(mockAI.getNextCheckpointIndex()).thenReturn(1);
        when(mockAI.getLapStartTime()).thenReturn(1000L);
        when(mockAI.getSectorTimes()).thenReturn(new long[]{100, 200, 300});
        when(mockAI.getSectorColors()).thenReturn(new Map.SectorColor[]{Map.SectorColor.NONE, Map.SectorColor.NONE, Map.SectorColor.NONE});

        when(mocktimeProvider.now()).thenReturn(4000L);

        controller.handleAICheckpoint(mockAI, 1);

        // Debe haber registrado el sector correspondiente
        verify(mockAI).recordSector(eq(0), anyLong());
    }

    @Test
    void testCheckpointtOutsideSectorRange() 
    {
        when(mockAI.getPassedCheckpoints()).thenReturn(new HashSet<>());
        when(mockAI.getNextCheckpointIndex()).thenReturn(4);

        controller.handleAICheckpoint(mockAI, 4);

        verify(mockAI, never()).recordSector(anyInt(), anyLong());
    }

    @Test
    void testCheckpointIncorrectReset() 
    {
        Set<Integer> passed = new HashSet<>(List.of(1, 2));
        when(mockAI.getPassedCheckpoints()).thenReturn(passed);
        when(mockAI.getNextCheckpointIndex()).thenReturn(3);
        when(mockMap.isRaceMode()).thenReturn(true);

        controller.handleAICheckpoint(mockAI, 0);

        assertTrue(passed.isEmpty());
        verify(mockAI).setNextCheckpointIndex(0);
    }

    @Test
    void testCheckpointIncorrect() 
    {
        Set<Integer> passed = new HashSet<>(List.of(1, 2));
        when(mockAI.getPassedCheckpoints()).thenReturn(passed);
        when(mockAI.getNextCheckpointIndex()).thenReturn(3);
        when(mockMap.isRaceMode()).thenReturn(false);

        controller.handleAICheckpoint(mockAI, 0);

        // No se reinician los checkpoints
        assertFalse(passed.isEmpty());
        verify(mockAI, never()).setNextCheckpointIndex(0);
    }

    @Test
    //Verificar que se suma el tiempo de sectores
    void testHandleAICheckpointSectors() 
    {
        when(mocktimeProvider.now()).thenReturn(2000L);

        when(mockAI.getLapStartTime()).thenReturn(1000L); 
        when(mockAI.getNextCheckpointIndex()).thenReturn(2); 
        when(mockAI.getSectorTimes()).thenReturn(new long[]{400L, 0L, 0L});
        when(mockAI.getPassedCheckpoints()).thenReturn(new HashSet<>()); 
        when(mockAI.getSectorColors()).thenReturn(new Map.SectorColor[]{Map.SectorColor.NONE, Map.SectorColor.NONE, Map.SectorColor.NONE});

        when(mockMap.isRaceMode()).thenReturn(false);
        when(mockMap.getBestSectorTime(anyInt())).thenReturn(Long.MAX_VALUE);

        controller.handleAICheckpoint(mockAI, 2);

        verify(mockAI).recordSector(eq(1), anyLong()); 
        verify(mockAI).getSectorTimes();               
        verify(mockAI).getLapStartTime();
        verify(mocktimeProvider, atLeastOnce()).now();
    }

    @Test
    //Verificación test checkpoint mayor al número de checkpoints
    void testGetDistanceToNextCheckpoint() 
    {
        when(mockAI.getX()).thenReturn(100.0);
        when(mockAI.getY()).thenReturn(100.0);

        when(controller.getNextCheckpointIndex(mockAI)).thenReturn(10);
        
        double distance = controller.getDistanceToNextCheckpoint(mockAI);
        assertTrue(distance >= 0);
    }


    @Test
    //Verificación funcionamiento coverage
    void testUpdateGlobalSectorTime() 
    {
        int sectorIndex = 0;
        long sectorTime = 500;
        long sectorTime2 = 1400;

        controller.setBestGlobalSectorTime(1000, sectorIndex);

        Map mockMap = mock(Map.class);
        AICar mockAI = mock(AICar.class);
        Map.SectorColor[] colors = new Map.SectorColor[3];
        when(mockAI.getSectorColors()).thenReturn(colors);
        when(mockMap.getBestSectorTime(sectorIndex)).thenReturn(400L, 1400L); 
        
        controller.updateGlobalSectorTime(sectorIndex, sectorTime, mockAI);

        assertEquals(Map.SectorColor.PURPLE, colors[sectorIndex]);
        assertEquals(500, controller.getBestGlobalSectorTimes()[sectorIndex]);
        verify(mockMap, never()).setBestSectorTime(anyInt(), anyLong()); 

        controller.updateGlobalSectorTime(sectorIndex, sectorTime2, mockAI);
        verify(mockMap, never()).setBestSectorTime(anyInt(), anyLong()); 
    }

    //Métodos de ayuda para poder hacer una especie de caballo de troya
    private Field getPrivateField(Object obj, String name) 
    {
        try {
            var field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            fail("Reflection failed for field: " + name);
            return null;
        }
    }

    private void setPrivateFieldValue(Object obj, Field field, Object value) 
    {
        try {
            field.set(obj, value);
        } catch (Exception e) {
            fail("Failed to set private field value: " + e.getMessage());
        }
    }

}
