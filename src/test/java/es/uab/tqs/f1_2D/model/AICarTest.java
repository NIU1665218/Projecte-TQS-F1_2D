package es.uab.tqs.f1_2D.model;


import es.uab.tqs.f1_2D.controlador.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.List;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Point;

public class AICarTest 
{
    private AICar aiCar;
    private Car playerCar;

    //Lista de argumentos para tests de parametrización
    static Stream<Arguments> skillLevelProvider() 
    {
        return Stream.of(
            //Skill Level Bajo [0.1 - 0.3]
            Arguments.of(0.1, 0.7, 120.0),  
            Arguments.of(0.2, 0.72, 115.0),
            Arguments.of(0.3, 0.74, 110.0), 
            
            //Skill Level Medio [0.4 - 0.6]
            Arguments.of(0.4, 0.76, 105.0), 
            Arguments.of(0.5, 0.78, 100.0), 
            Arguments.of(0.6, 0.8, 95.0),   
            
            //Skill Level Alto [0.7 - 1.0]
            Arguments.of(0.7, 0.82, 90.0), 
            Arguments.of(0.85, 0.87, 85.0), 
            Arguments.of(1.0, 0.9, 80.0)   
        );
    }

    static Stream<Arguments> slipstreamScenarioProvider() 
    {
        return Stream.of(
            //Sin slipstream
            Arguments.of(1, 100.0, 0.0, "No slipstream en vuelta 1"),
            
            //Slipstream posible 
            Arguments.of(2, 200.0, 0.0, "Slipstream posible pero sin objetivo"),
            
            //Slipstream activo
            Arguments.of(3, 50.0, 5.0, "Slipstream activo con coche cercano")
        );
    }

    static Stream<Arguments> pairwiseParameters() 
    {
        return Stream.of(
            // Combinaciones pairwise para skill level, posición, y estado de carrera
            Arguments.of(0.2, 100.0, 100.0, 1, "Baja skill, posición normal, vuelta 1"),
            Arguments.of(0.2, 0.0, 0.0, 3, "Baja skill, esquina, vuelta 3"),
            Arguments.of(0.8, 100.0, 0.0, 1, "Alta skill, borde superior, vuelta 1"),
            Arguments.of(0.8, 0.0, 100.0, 3, "Alta skill, borde inferior, vuelta 3"),
            Arguments.of(0.5, 500.0, 500.0, 2, "Skill media, centro, vuelta 2"),
            Arguments.of(0.5, 0.0, 0.0, 1, "Skill media, esquina, vuelta 1"),
            Arguments.of(1.0, 100.0, 100.0, 3, "Máxima skill, normal, vuelta 3"),
            Arguments.of(0.1, 999.0, 999.0, 2, "Mínima skill, esquina opuesta, vuelta 2")
        );
    }

    static Stream<Arguments> teamAndSkillPairwise() {
        return Stream.of(
            Arguments.of("Mercedes", 0.9, "Equipo top, alta skill"),
            Arguments.of("Mercedes", 0.3, "Equipo top, baja skill"),
            Arguments.of("Haas", 0.9, "Equipo bajo, alta skill"),
            Arguments.of("Haas", 0.3, "Equipo bajo, baja skill"),
            Arguments.of("Ferrari", 0.6, "Equipo medio, skill media"),
            Arguments.of("Williams", 0.7, "Equipo medio-bajo, skill media-alta")
        );
    }


    @BeforeEach
    void setUp() 
    {
        playerCar = new Car(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000);
        aiCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Mercedes", 0.8);
        aiCar.setPlayerCar(playerCar);
    }

    
    /* 
    =============================================================================
    TDD - Test Driven Development
    =============================================================================
    */

    @Test
    //AI Car debe crearse con parámetros específicos de equipo y skill
    void testAICarCreation() 
    {
        assertEquals("Mercedes", aiCar.getTeam());
        assertEquals(0.8, aiCar.getSkillLevel());
        assertNotNull(aiCar.getRacingLine());
    }

    @Test
    // AI Car debe calcular distancia correctamente a otro coche
    void testDistanceCalculation()
    {
        AICar otherCar = new AICar(150, 150, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Ferrari", 0.7);

        double distance = aiCar.calculateDistance(otherCar);

        double expectedDistance = Math.sqrt(Math.pow(50, 2) + Math.pow(50, 2));
        assertEquals(expectedDistance, distance, 0.1);
    }

    @Test
    //AI Car debe detectar correctamente slipstream en vuelta 2+
    void testSlipstreamDetection() 
    {
        AICar aheadCar = new AICar(120, 120, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Ferrari", 0.7);
        aiCar.applySlipstream(List.of(aheadCar), 2);
    
        assertTrue(aiCar.getSlipstreamBoost() > 0);
    }

    @Test
    //AI Controller debe inicializar 19 coches con diferentes skills
    void testAIControllerInitialization() 
    {
        Map mockMap = mock(Map.class);
        when(mockMap.getFinishLine()).thenReturn(new Rectangle(100, 100, 50, 50));
        
        AIController controller = new AIController(mockMap, playerCar);

        assertEquals(19, controller.getAICars().size());

        long topDrivers = controller.getAICars().stream()
            .filter(car -> car.getSkillLevel() >= 0.7)
            .count();
        assertEquals(5, topDrivers);
    }

    /* 
    =============================================================================
    Caja Negra
    =============================================================================
    */

    @ParameterizedTest
    @MethodSource("skillLevelProvider")
    //Comportamiento según particiones de skill level
    void testAICarSkillLevel(double skillLevel, double expectedCorneringSpeed, double expectedBrakingDistance) 
    {
        AICar aiCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Test", skillLevel);
        assertTrue(aiCar.getSkillLevel() >= 0.1 && aiCar.getSkillLevel() <= 1.0);
    }

    @ParameterizedTest
    @MethodSource("slipstreamScenarioProvider")
    //Comportamiento slipstream
    void testSlipstream(int currentLap, double distance, double expectedBoost, String scenario) 
    {
        AICar aiCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Test", 0.8);
        AICar otherCar = mock(AICar.class);
        when(otherCar.getX()).thenReturn(100.0 + distance);
        when(otherCar.getY()).thenReturn(100.0);
        
        aiCar.applySlipstream(List.of(otherCar), currentLap);

        if (currentLap >= 2 && distance < 80) {
            assertTrue(aiCar.getSlipstreamBoost() > 0, scenario);
        } else {
            assertEquals(0.0, aiCar.getSlipstreamBoost(), 0.1, scenario);
        }
    }

    @Test
    //Skill level en límites extremos
    void testSkillLevelBoundaries() 
    {
        //Límite inferior
        AICar minSkillCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Min", 0.1);
        assertEquals(0.1, minSkillCar.getSkillLevel());
        
        //Límite superior
        AICar maxSkillCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Max", 1.0);
        assertEquals(1.0, maxSkillCar.getSkillLevel());
        
        //Fuera de límites
        AICar belowMinCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Below", -0.1);
        assertEquals(0.1, belowMinCar.getSkillLevel());
        
        AICar aboveMaxCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Above", 1.5);
        assertEquals(1.0, aboveMaxCar.getSkillLevel());
    }

    @Test
    //Distancias límite detección de slipstream
    void testSlipstreamDistanceBoundaries() 
    {
        AICar aiCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Test", 0.8);
        
        //Distancia justo por debajo (99.9)
        AICar nearCar = mock(AICar.class);
        when(nearCar.getX()).thenReturn(100.0 + 99.9);
        when(nearCar.getY()).thenReturn(100.0);
        
        aiCar.applySlipstream(List.of(nearCar), 2);
        assertTrue(aiCar.getSlipstreamBoost() > 0);
        
        //Distancia justo por encima (100.1)
        AICar farCar = mock(AICar.class);
        when(farCar.getX()).thenReturn(100.0 + 100.1);
        when(farCar.getY()).thenReturn(100.0);
        
        aiCar.applySlipstream(List.of(farCar), 2);
        assertEquals(0.0, aiCar.getSlipstreamBoost(), 0.1);
    }

    @Test
    //Límites de vuelta para activación de slipstream
    void testSlipstreamLapBoundaries() {
        AICar aiCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Test", 0.8);
        AICar otherCar = mock(AICar.class);
        when(otherCar.getX()).thenReturn(120.0);
        when(otherCar.getY()).thenReturn(100.0);
        
        //Primera vuelta - No activo
        aiCar.applySlipstream(List.of(otherCar), 1);
        assertEquals(0.0, aiCar.getSlipstreamBoost(), 0.1);
        
        //Segunda vuelta - Activo
        aiCar.applySlipstream(List.of(otherCar), 2);
        assertTrue(aiCar.getSlipstreamBoost() > 0);
    }

    @Test
    //Comportamiento en bordes del mapa
    void testMapBoundaryBehavior() 
    {
        AICar topLeftCar = new AICar(0, 0, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "TopLeft", 0.5);
        AICar bottomRightCar = new AICar(999, 999, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "BottomRight", 0.5);
        
        assertTrue(topLeftCar.getX() >= 0);
        assertTrue(topLeftCar.getY() >= 0);
        assertTrue(bottomRightCar.getX() <= 1000);
        assertTrue(bottomRightCar.getY() <= 1000);
    }

    
    @ParameterizedTest
    @MethodSource("pairwiseParameters")
    //Combinaciones pairwise de parámetros de AI
    void testPairwiseCombinations(double skillLevel, double x, double y, int currentLap, String description) {
        
        AICar aiCar = new AICar(x, y, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Test", skillLevel);
        
        List<Point> racingLine = List.of(
            new Point((int)x + 50, (int)y + 50),
            new Point((int)x + 100, (int)y + 100)
        );
        aiCar.setRacingLine(racingLine);
        
        assertDoesNotThrow(() -> {
            BufferedImage mockCollisionMap = mock(BufferedImage.class);
            when(mockCollisionMap.getRGB(anyInt(), anyInt())).thenReturn(0);
            aiCar.updateAI(mockCollisionMap, new ArrayList<>(), currentLap);
        }, description);
    }

    @ParameterizedTest
    @MethodSource("teamAndSkillPairwise")
    //Combinaciones equipo-skill level
    void testTeamAndSkillCombinations(String team, double skillLevel, String description) {

        AICar aiCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, team, skillLevel);
        
        assertEquals(team, aiCar.getTeam(), description);
        assertEquals(skillLevel, aiCar.getSkillLevel(), 0.01, description);
        
        assertTrue(aiCar.getSkillLevel() >= 0.1);
        assertTrue(aiCar.getSkillLevel() <= 1.0);
    }

/* 
=============================================================================
  Mock Objects
=============================================================================
*/

    @Test
    //AI Car con mock de colisión debe actualizar posición correctamente
    void testAICarUpdateWithMockCollision() {
        
        BufferedImage mockCollisionMap = mock(BufferedImage.class);
        when(mockCollisionMap.getRGB(anyInt(), anyInt())).thenReturn(0); // Color de pista válido
        
        AICar aiCar = new AICar(100, 100, 0, 0, 15, -5, 2, 0.5, 1000, 1000, "Red Bull", 0.9);
        List<java.awt.Point> racingLine = List.of(
            new java.awt.Point(150, 150),
            new java.awt.Point(200, 200)
        );
        aiCar.setRacingLine(racingLine);
        aiCar.updateAI(mockCollisionMap, new ArrayList<>(), 1);
        
        verify(mockCollisionMap, atLeastOnce()).getRGB(anyInt(), anyInt());
        assertNotEquals(100, aiCar.getX());
    }
}
