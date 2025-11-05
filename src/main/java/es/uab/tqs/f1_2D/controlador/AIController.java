package es.uab.tqs.f1_2D.controlador;

import es.uab.tqs.f1_2D.model.AICar;
import es.uab.tqs.f1_2D.model.Car;
import es.uab.tqs.f1_2D.model.Map;
import es.uab.tqs.f1_2D.model.TimeProvider;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.*;

public class AIController 
{
    private List<AICar> aiCars;
    private Map trackMap;
    private List<Point> racingLine;
    private Car playerCar;
    private long[] bestGlobalSectorTimes;
    private TimeProvider timeProvider;
    private RandomGenerator randomGenerator;
    private List<AICar> positions;
    
    //Nombres de los equipos existentes
    private final String[] F1_TEAMS = 
    {
        "Mercedes", "Red Bull", "McLaren", "Alpine", 
        "Racing Bulls", "Aston Martin", "Williams", "Sauber", "Haas"
    };
    
    public AIController(Map trackMap, Car playerCar) 
    {
        //Inicializar las variables necesarias
        this.trackMap = trackMap;
        this.playerCar = playerCar;
        this.aiCars = new ArrayList<>();
        this.racingLine = new ArrayList<>();
        this.bestGlobalSectorTimes = new long[3]; 
        this.timeProvider = System::currentTimeMillis; 
        this.randomGenerator = new RandomGenerator();
        Arrays.fill(bestGlobalSectorTimes, Long.MAX_VALUE);
        
        //Generar la racing line para que sigan los coches
        generateRacingLine();

        //Inicializar los 19 coches
        initializeAICars();
    }

    public AIController(Map trackMap, Car playerCar, RandomGenerator randomGenerator) 
    {
        //Inicializar las variables necesarias
        this.trackMap = trackMap;
        this.playerCar = playerCar;
        this.aiCars = new ArrayList<>();
        this.racingLine = new ArrayList<>();
        this.bestGlobalSectorTimes = new long[3]; 
        this.timeProvider = System::currentTimeMillis; 
        this.randomGenerator = randomGenerator;
        Arrays.fill(bestGlobalSectorTimes, Long.MAX_VALUE);
        
        //Generar la racing line para que sigan los coches
        generateRacingLine();

        //Inicializar los 19 coches
        initializeAICars();
    }
    
    //Generar la racing line
    private void generateRacingLine() 
    {
        racingLine.clear(); 
        
        // Waypoints principales 
        Point[] keyPoints = {
            new Point(431, 1166), 
            new Point(600, 823), 
            new Point(817, 585), //T1
            new Point(1240, 668), 
            new Point(1728, 760), 
            new Point(2472, 872), 
            new Point(3152, 976), 
            new Point(3888, 988), //T2
            new Point(4548, 1056), 
            new Point(4816, 928), 
            new Point(4908, 780), //T3
            new Point(4860, 632), 
            new Point(4762, 462), //T4
            new Point(4888, 303), 
            new Point(5032, 252), 
            new Point(5324, 196), 
            new Point(5632, 136), //T5
            new Point(5752, 208), 
            new Point(5752, 372), 
            //HORQUILLA 
            new Point(5800, 520),  
            new Point(5868, 569),  
            new Point(5920, 592),  
            new Point(5970, 598),    
            new Point(6028, 585), //T6
            new Point(6070, 547),  
            new Point(6095, 472),  
            new Point(6110, 407), //T7 
            new Point(6120, 355),  
            new Point(6180, 345),
            new Point(6227, 335),        
            new Point(6296, 330), 
            new Point(6369, 335),
            new Point(6436, 360),
            new Point(6490, 418), //T8
            new Point(6537, 477),
            new Point(6500, 664), 
            new Point(6408, 840), 
            new Point(6196, 1020), 
            new Point(5808, 1180), //T9
            new Point(5032, 1236), 
            new Point(3648, 1200), 
            new Point(3420, 1264), //T10
            new Point(3292, 1332), 
            new Point(2976, 1268), //T11
            new Point(2128, 1040), 
            new Point(1856, 1024), //T12
            new Point(1492, 1220), //T13, T14 se encuentra en medio
            new Point(1612, 1810), //T15
            new Point(1494, 2006),
            new Point(1412, 2064), 
            new Point(1369, 2142), //T16
            new Point(1492, 2288), 
            new Point(1749, 2356), //T17
            new Point(1861, 2424),
            new Point(1880, 2636), //T18
            new Point(1734, 2700),
            new Point(1496, 2733), 
            new Point(760, 2708), 
            new Point(640, 2616), //T19
            new Point(472, 2232), 
            new Point(344, 1744), 
            new Point(360, 1480), 
            new Point(444, 1344)
        };
        
        // Añadir waypoints intermedios para mejor navegación
        for (int i = 0; i < keyPoints.length - 1; i++) {
            Point current = keyPoints[i];
            Point next = keyPoints[i + 1];
            
            // Añadir punto actual
            racingLine.add(current);
            
            // Añadir puntos intermedios si la distancia es grande
            double distance = Math.sqrt(Math.pow(next.x - current.x, 2) + Math.pow(next.y - current.y, 2));
            //Si la distancia es mayor a 150 píxeles añadir uno cada 75 píxeles
            if (distance > 150) 
            {
                int intermediatePoints = (int)(distance / 75); 
                for (int j = 1; j < intermediatePoints; j++) {
                    double t = (double)j / intermediatePoints;
                    int interX = (int)(current.x + (next.x - current.x) * t);
                    int interY = (int)(current.y + (next.y - current.y) * t);
                    racingLine.add(new Point(interX, interY));
                }
            }
        }
        
        // Añadir el último punto
        racingLine.add(keyPoints[keyPoints.length - 1]);
        
    }
    
    //Inicializar los 19 coches
    private void initializeAICars() 
    {
        int numberOfCars = 19;
        
        // Listas personalizables de posiciones X e Y para la parrilla de salida
        double[] startPositionsX = {
            444, 
            360, 428, 
            336, 420, 
            332, 424, 
            324, 432, 
            340, 444, 
            356, 464,
            384, 496, 
            412, 496, 
            436, 502
        };
        
        double[] startPositionsY = {
            1344, 
            1376, 1448, 
            1476, 1544, 
            1572, 1644, 
            1676, 1736, 
            1776, 1836, 
            1872, 1936, 
            1976, 2036, 
            2076, 2132, 
            2176, 2194
        };
        
        for (int i = 0; i < numberOfCars; i++) 
        { 
            //Determinar equipo del coche
            String team = F1_TEAMS[i % F1_TEAMS.length];
            
            double skillLevel;
            
            //Determinar el skill level de ese coche, los primeros 5 coches tendrán mejor skill
            if (i < 5) skillLevel = 0.8 + randomGenerator.nextDouble() * 0.3; 
            else skillLevel = 0.65 + randomGenerator.nextDouble() * 0.3;  

            // Usar las posiciones definidas en las listas
            double startX = startPositionsX[i];
            double startY = startPositionsY[i];
            
            // Calcular ángulo hacia el primer waypoint
            double startAngle = calculateAngleToWaypoint(startX, startY, 1);

            double baseMaxVelocity = 30 + (randomGenerator.nextDouble() * 4);
            
            //Inicializar el coche
            AICar aiCar = new AICar(
                startX, startY, startAngle, 3.0, 
                baseMaxVelocity, -5, 0.2, 0.5,
                trackMap.getMapHeight(), trackMap.getMapWidth(),
                team, skillLevel
            );

            //Adjudicarle la racing line, el coche del jugador y añadirlo a la lista de coches rivales
            aiCar.setRacingLine(new ArrayList<>(racingLine));
            aiCar.setPlayerCar(playerCar);
            aiCars.add(aiCar);
        }
    }

    //Calcular el angulo de salida 
    public double calculateAngleToWaypoint(double startX, double startY, int waypointIndex) 
    {
        //Ángulo por default si falla la racing line
        if (racingLine.size() <= waypointIndex) return 280; 
        
        //Calcular el ángulo
        Point waypoint = racingLine.get(waypointIndex);
        double dx = waypoint.x - startX;
        double dy = waypoint.y - startY;
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        
        // Asegurar que el ángulo esté en el rango correcto
        if (angle < 0) 
        {
            angle += 360;
        }

        return angle;
    }
    
    //Función principal para actualizar la posición de los coches rivales
    public void updateAllAI(BufferedImage collisionMap) 
    {
        int currentLap = trackMap.getCurrentLap();
        
        for (AICar aiCar : aiCars)
        {
            aiCar.updateAI(collisionMap, aiCars, currentLap);
            updateAICheckpoints(aiCar);
            aiCar.updateLapTime();
        }

        updatePositions();
    }
    
    //Verificar si se encuentra en la finish line o encima de un checkpoint
    private void updateAICheckpoints(AICar aiCar)
    {
        //Verificar línea de meta
        if (trackMap.getFinishLine().contains((int)(aiCar.getX() + 40), (int)(aiCar.getY() + 40))) 
        {
            handleAIFinishLine(aiCar);
            return; 
        }
        
        //Verificar checkpoints normales
        var checkpoints = trackMap.getCheckpoints();
        for (int i = 0; i < checkpoints.size(); i++) 
        {
            Rectangle checkpoint = checkpoints.get(i);
            if (checkpoint.contains((int)(aiCar.getX() + 40), (int)(aiCar.getY() + 40))) 
            {
                handleAICheckpoint(aiCar, i);
                break; 
            }
        }
    }
    
    //Si ha pasado por línea de meta, verificar si debe empezar una nueva vuelta o terminar la carrera
    public void handleAIFinishLine(AICar aiCar) {
        
        // Si es la primera vez que pasa o ha completado todos los checkpoints
        if (aiCar.getLapStartTime() == 0 || aiCar.passedAllCheckpoints()) 
        {
            //Si el tiempo es mayor a 0, verificar si debe aumentar una vuelta o finalizar la carrera
            if (aiCar.getLapStartTime() > 0) 
            {
                aiCar.completeLap();
            }
            //De todos modos empezar una nueva vuelta y resetar los valores
            aiCar.startNewLap();
        }
    }

    //Si ha pasado por un checkpoint, controlar sectores y tiempos
    public void handleAICheckpoint(AICar aiCar, int checkpointIndex) 
    {
        // Si ya ha pasado por el checkpoint, lo ignora
        if (aiCar.getPassedCheckpoints().contains(checkpointIndex)) 
        {
            return;
        }
        
        // Verificar si es el checkpoint correcto
        if (checkpointIndex == aiCar.getNextCheckpointIndex()) 
        {
            aiCar.getPassedCheckpoints().add(checkpointIndex);
            aiCar.setNextCheckpointIndex(aiCar.getNextCheckpointIndex() + 1);
                        
            // Grabar sector 
            if (checkpointIndex >= 1 && checkpointIndex <= 3) 
            {
                //Calcular tiempo de sector
                int sectorIndex = checkpointIndex - 1;
                long now = timeProvider.now();
                long totalSinceStart = now - aiCar.getLapStartTime();
                
                // Calcular suma de sectores anteriores
                long prevSum = 0;
                for (int i = 0; i < sectorIndex; i++) 
                {
                    prevSum += aiCar.getSectorTimes()[i];
                }
                
                //Grabar tiempo de sector
                long sectorTime = totalSinceStart - prevSum;
                aiCar.recordSector(sectorIndex, sectorTime);
                
                // Actualizar mejor sector global
                updateGlobalSectorTime(sectorIndex, sectorTime, aiCar);
            }
        } 
        else 
        {
            // Checkpoint incorrecto, reiniciar
            if (trackMap.isRaceMode()) 
            {
                aiCar.getPassedCheckpoints().clear();
                aiCar.setNextCheckpointIndex(0);
            }
        }
    }
    
    //Si tiene el mejor tiempo de sector de todos los coches, asignarle sector en morado
    public void updateGlobalSectorTime(int sectorIndex, long sectorTime, AICar aiCar) 
    {
        if (sectorTime < bestGlobalSectorTimes[sectorIndex]) 
        {
            bestGlobalSectorTimes[sectorIndex] = sectorTime;
            
            //Actualizar color a PURPLE para este coche
            aiCar.getSectorColors()[sectorIndex] = Map.SectorColor.PURPLE;
            
            if (sectorTime < trackMap.getBestSectorTime(sectorIndex)) 
            {
                trackMap.setBestSectorTime(sectorIndex, sectorTime);
            }
        }
    }

    //Resetar todos los valores para empezar de nuevo una carrera
    public void reset()
    {
        aiCars.clear();
        initializeAICars();
        Arrays.fill(bestGlobalSectorTimes, Long.MAX_VALUE);
        positions = null;
    }
    
    //Sistema de posiciones
    public void updatePositions() 
    {
        //Crear lista con todos los coches y el jugador
        List<Car> allCars = new ArrayList<>();
        allCars.add(playerCar);
        allCars.addAll(aiCars);
        
        //Ordenar por posición
        allCars.sort((c1, c2) -> {
            int lap1 = (c1 instanceof AICar) ? ((AICar)c1).getCurrentLap() : trackMap.getCurrentLap();
            int lap2 = (c2 instanceof AICar) ? ((AICar)c2).getCurrentLap() : trackMap.getCurrentLap();
            
            //Más vueltas primero
            if (lap1 != lap2) 
            {
                return Integer.compare(lap2, lap1);
            }
            
            //Misma vuelta, comparar por checkpoints pasados
            int checkpoints1 = getCheckpointsPassed(c1);
            int checkpoints2 = getCheckpointsPassed(c2);
            
            //Más checkpoints primero
            if (checkpoints1 != checkpoints2) 
            {
                return Integer.compare(checkpoints2, checkpoints1); 
            }
            
            //Mismos checkpoints, comparar por distancia al siguiente checkpoint
            double distance1 = getDistanceToNextCheckpoint(c1);
            double distance2 = getDistanceToNextCheckpoint(c2);
            
            //Menor distancia primero
            return Double.compare(distance1, distance2); 
        });
        
        //Actualizar lista de posiciones 
        positions = new ArrayList<>();
        for (Car car : allCars) 
        {
            if (car instanceof AICar) 
            {
                positions.add((AICar) car);
            }
        }
    }

    //Calcular la distancia al siguiente checkpoint
    public double getDistanceToNextCheckpoint(Car car) 
    {
        //Definir todos los checkpoints
        List<Rectangle> allCheckpoints = Arrays.asList(
            new Rectangle(4548, 972, 40, 200),  
            new Rectangle(5484, 152, 20, 200),    
            new Rectangle(1388, 1216, 250, 50), 
            new Rectangle(432, 2344, 250, 50),  
            new Rectangle(360, 1200, 200, 40)   
        );
        
        //Obtener el siguiente checkpoint a pasar por
        int nextCheckpointIndex = getNextCheckpointIndex(car);
        
        //Si ya ha pasado por todos los checkpoints, se considera la distancia a la línea de meta
        if (nextCheckpointIndex >= allCheckpoints.size()) {
            nextCheckpointIndex = allCheckpoints.size() - 1; 
        }
        
        Rectangle nextCheckpoint = allCheckpoints.get(nextCheckpointIndex);
        
        // Calcular distancia desde el centro del coche al centro del checkpoint
        double carCenterX = car.getX() + 40;
        double carCenterY = car.getY() + 40;
        double checkpointCenterX = nextCheckpoint.getX() + nextCheckpoint.getWidth() / 2.0;
        double checkpointCenterY = nextCheckpoint.getY() + nextCheckpoint.getHeight() / 2.0;
        
        return Math.sqrt(Math.pow(checkpointCenterX - carCenterX, 2) + 
                        Math.pow(checkpointCenterY - carCenterY, 2));
    }

    //Getter para obtener el número de checkpoints pasados
    private int getCheckpointsPassed(Car car) 
    {
        if (car instanceof AICar) 
        {
            AICar aiCar = (AICar) car;
            return aiCar.getPassedCheckpoints().size();
        } 
        else 
        {
            return trackMap.getPassedCheckpoints().size();
        }
    }

    //Getter del siguiente checkpoint
    public int getNextCheckpointIndex(Car car) 
    {
        if (car instanceof AICar) 
        {
            AICar aiCar = (AICar) car;
            return aiCar.getNextCheckpointIndex();
        } 
        else 
        {
            return trackMap.getNextCheckpointIndex();
        }
    }
    
    //Getter para posiciones
    public List<AICar> getPositions() 
    {
        if (positions == null) 
        {
            updatePositions();
        }
        return positions;
    }
    
    //Getter de la posición de un coche
    public int getAIPosition(AICar aiCar) 
    {
        List<AICar> positions = getPositions();
        return positions.indexOf(aiCar) + 1;
    }
    
    //Getters
    public long[] getBestGlobalSectorTimes() {return bestGlobalSectorTimes;}
    public List<AICar> getAICars() {return aiCars;}
    public List<Point> getRacingLine() {return racingLine;}
    
    //Setters
    public void setAIDifficulty(double globalDifficulty) 
    {
        for (AICar aiCar : aiCars) 
        {
            double currentSkill = aiCar.getSkillLevel();
            double newSkill = currentSkill * globalDifficulty;
            aiCar.setSkillLevel(Math.min(1.0, newSkill));
        }
    }
    public void setTimeProvider(TimeProvider timeProvider) {this.timeProvider = timeProvider;}
    public void setBestGlobalSectorTime(int time, int sectorIndex) {this.bestGlobalSectorTimes[sectorIndex] = time;}
}