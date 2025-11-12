    package es.uab.tqs.f1_2D.model;

    import java.awt.image.BufferedImage;
    import java.awt.Point;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;

    public class AICar extends Car 
    {
        private String team;
        private double skillLevel; 
        private List<Point> racingLine;
        private int currentTargetIndex;
        private double targetSpeed;
        private Car playerCar;
        private double slipstreamBoost;
        private double brakingDistance;

        private long lapStartTime;
        private long bestLapTime;
        private long lapTime;
        private int currentLap;
        private TimeProvider timeProvider;
        
        private final int numSectors = 3;
        private long[] sectorTimes;
        private long[] bestSectorTimes;
        private Map.SectorColor[] sectorColors;
        private boolean[] sectorRecorded;
        
        private Set<Integer> passedCheckpoints;
        private int nextCheckpointIndex;
        
        public AICar(double x, double y, double angle, double velocity, double maxVelocity, 
                    double backwardsMaxVelocity, double acceleration, double backwardsAcceleration, 
                    int mapHeight, int mapWidth, String team, double skillLevel) 
        {
            //Llamada a clase base Car para inicializar la mayoria de variables
            super(x, y, angle, velocity, maxVelocity, backwardsMaxVelocity, acceleration, 
                backwardsAcceleration, mapHeight, mapWidth);
            
            //Inicializar el resto de variables
            this.team = team;
            this.skillLevel = Math.max(0.1, Math.min(1.0, skillLevel));
            this.racingLine = new ArrayList<>();
            this.currentTargetIndex = 0;
            this.slipstreamBoost = 0;
            this.setTimeProvider(System::currentTimeMillis);
            this.currentLap = 0;
            this.bestLapTime = Long.MAX_VALUE;

            this.sectorTimes = new long[numSectors];
            this.bestSectorTimes = new long[numSectors];
            this.sectorColors = new Map.SectorColor[numSectors];
            this.sectorRecorded = new boolean[numSectors];
            this.passedCheckpoints = new HashSet<>();
            this.nextCheckpointIndex = 0; 
            
            Arrays.fill(bestSectorTimes, Long.MAX_VALUE);
            Arrays.fill(sectorColors, Map.SectorColor.NONE);
            Arrays.fill(sectorRecorded, false);
            Arrays.fill(sectorTimes, Long.MAX_VALUE);
            
            //Inicializar variables que dependen de skill level
            setupAIParameters();
        }
        
        //Inicialización de varaibles dependientes del nivel de los AICars
        private void setupAIParameters() 
        {
            this.brakingDistance = 150 + (100 * (1 - skillLevel)); 
            this.targetSpeed = maxVelocity * (0.8 + (0.2 * skillLevel));
        }
         
        //Función principal para actualizar la posición de los coches rivales
        public void updateAI(BufferedImage collisionMap, List<AICar> otherAICars, int currentLap)
        {       
            if (racingLine.isEmpty()) return;
            
            applySlipstream(otherAICars, currentLap);
            followRacingLine();
            update(collisionMap);
        }
        
        //Aplicar Slipstream a los coches rivales
        public void applySlipstream(List<AICar> otherAICars, int currentLap) 
        {
            slipstreamBoost = 0;
            
            //Solo aplicar a partir de la segunda vuelta 
            if (currentLap >= 2) 
            {
                // Buscar coches cercanos por delante
                for (AICar otherCar : otherAICars) 
                {
                    //Si el coche se encuentra justo detrás
                    if (isBehind(otherCar)) 
                    {
                        //Calcular la distancia hasta el coche
                        double distance = calculateDistance(otherCar);

                        //Si la distancia es menor a 100 pixeles, aplicar slipstream
                        if (distance < 100) 
                        { 
                             slipstreamBoost = 5.0 * skillLevel; 
                        }
                    }
                }
                
                //Si se encuentra detrás del jugador
                if (isBehindPlayer()) 
                {
                    //Calcular la distancia al jugador
                    double distance = calculateDistanceToPlayer();

                    //Si la distancia es menor a 100 aplicar slipstream
                    if (distance < 100) 
                    {
                        slipstreamBoost = Math.max(slipstreamBoost, 5.0 * skillLevel);
                    }
                }
            }
        }
        
        //Función principal para seguir la Racing Line (no al pie de la letra) para saber por donde ir
        public void followRacingLine() 
        {
            //Resetear indice para el principio de vueltas
            if (currentTargetIndex >= racingLine.size()) 
            {
                currentTargetIndex = 0;
            }
            
            //Obtener el siguiente indice que debe seguir el coche
            Point target = racingLine.get(currentTargetIndex);
            double targetX = target.x;
            double targetY = target.y;
            
            double dx = targetX - x;
            double dy = targetY - y;

            //Calcular la distancia hasta el siguiente punto
            double distanceToTarget = Math.sqrt(dx * dx + dy * dy);

            //Si el skill level es inferior, mirar más adelante los checkpoints para evitar chocarse
            if(skillLevel < 0.6)
            {
                //Si estamos muy cerca del waypoint, pasar al siguiente
                if (distanceToTarget < 110)  
                {
                    currentTargetIndex++;

                    //Tener en cuenta que el siguiente puede ser la línea de meta
                    if(currentTargetIndex == racingLine.size()) currentTargetIndex = 0;

                    //Como se ha cambiado el siguiente punto, cambiar rumbo y calcular distancia
                    target = racingLine.get(currentTargetIndex);
                    targetX = target.x;
                    targetY = target.y;
                    
                    dx = targetX - x;
                    dy = targetY - y;
                    distanceToTarget = Math.sqrt(dx * dx + dy * dy);
                
                }
            }
            //Si tienen un skill level más alto, no les hace falta tanta distancia de detección
            else
            {
                //Si estamos muy cerca del waypoint, pasar al siguiente
                if (distanceToTarget < 90)  
                {
                    currentTargetIndex++;

                    //Tener en cuenta que el siguiente puede ser la línea de meta
                    if(currentTargetIndex == racingLine.size()) currentTargetIndex = 0;
                    
                    //Como se ha cambiado el siguiente punto, cambiar rumbo y calcular distancia
                    target = racingLine.get(currentTargetIndex);
                    targetX = target.x;
                    targetY = target.y;
                    
                    dx = targetX - x;
                    dy = targetY - y;
                    distanceToTarget = Math.sqrt(dx * dx + dy * dy);
                
                }
            }
            
            //Calcular ángulo hacia el siguiente objetivo
            double targetAngle = Math.toDegrees(Math.atan2(dy, dx));
            
            //Suavizar el giro en función del skill level (más giro o menos giro)
            double angleDiff = normalizeAngle(targetAngle - angle);
            double turnAmount = Math.min(5.0 * skillLevel, Math.abs(angleDiff)); 
            
            //Decidir dirección de giro
            if (angleDiff > 0) 
            {
                angle += turnAmount;
            } 
            else 
            {
                angle -= turnAmount;
            }
            
            //Control de velocidad 
            double speedMultiplier = 1.0;
            
            //Reducir velocidad en curvas pronunciadas
            if (Math.abs(angleDiff) > 90) 
            {
                speedMultiplier = 0.4;
            } 
            else if (Math.abs(angleDiff) > 45) 
            {
                speedMultiplier = 0.6;
            } 
            else if (Math.abs(angleDiff) > 20) 
            {
                speedMultiplier = 0.8;
            }
            
            //Reducir velocidad al acercarse al waypoint
            if (distanceToTarget < brakingDistance)
            {
                double brakeMultiplier = Math.max(0.3, distanceToTarget / brakingDistance);
                speedMultiplier *= brakeMultiplier;
            }
            
            double currentTargetSpeed = (targetSpeed + slipstreamBoost) * speedMultiplier;
            
            //Determinar si el coche debe frenar o acelerar
            double speedDifference = currentTargetSpeed - velocity;
            
            //Deicidir si el coche debe frenar o acelerar según su situación
            if (speedDifference > 0) 
            {
                
                velocity += acceleration * skillLevel * Math.min(1.0, speedDifference / 5.0);
            } 
            else 
            {
                
                velocity += backwardsAcceleration * skillLevel * Math.max(-1.0, speedDifference / 5.0);
            }
            
            // Limitar velocidad máxima y mínima
            velocity = Math.max(backwardsMaxVelocity, Math.min(getEffectiveMaxVelocity(), velocity));
        }
                
        //Calcular distancia hasta otro coche
        public double calculateDistance(AICar otherCar) 
        {
            double dx = otherCar.getX() - x;
            double dy = otherCar.getY() - y;
            return Math.sqrt(dx * dx + dy * dy);
        }
        
        //Calcular distancia hasta el coche del jugador
        private double calculateDistanceToPlayer() 
        {
            double dx = playerCar.getX() - x;
            double dy = playerCar.getY() - y;
            return Math.sqrt(dx * dx + dy * dy);
        }
        
        //Determinar si un coche se encuentra detrás o no
        private boolean isBehind(AICar otherCar) 
        {
            if(otherCar == this) return false;
            double dx = otherCar.getX() - x;
            double dy = otherCar.getY() - y;
            double relativeAngle = Math.toDegrees(Math.atan2(dy, dx));
            double angleDiff = normalizeAngle(relativeAngle - angle);
            return Math.abs(angleDiff) < 60; 
        }
        
        //Determinar si un coche se encuentra detrás del cotxes del jugador
        private boolean isBehindPlayer() 
        {
            if (playerCar == null) return false;
            double dx = playerCar.getX() - x;
            double dy = playerCar.getY() - y;
            double relativeAngle = Math.toDegrees(Math.atan2(dy, dx));
            double angleDiff = normalizeAngle(relativeAngle - angle);
            return Math.abs(angleDiff) < 60;
        }
        
        //Normalizar los cálculos de los ángulos para controlar el valor entre 0-360
        public double normalizeAngle(double angle) 
        {
            while (angle > 180) angle -= 360;
            while (angle < -180) angle += 360;
            return angle;
        }

        //Resetear todos los valores necesarios para poder empezar una nueva vuelta
        public void startNewLap() 
        {
            this.lapStartTime = timeProvider.now();
            Arrays.fill(sectorTimes, Long.MAX_VALUE);
            Arrays.fill(sectorRecorded, false);
            Arrays.fill(sectorColors, Map.SectorColor.NONE);
            passedCheckpoints.clear();
            nextCheckpointIndex = 0;
            currentLap++;
        }
        
        //Guardar el tiempo de sector realizado por un coche AI
        public void recordSector(int sectorIndex, long sectorTime) 
        {
            //Verificiar que el sector existe
            if (sectorIndex < 0 || sectorIndex >= numSectors) return;
            
            //Asignar el valor
            sectorTimes[sectorIndex] = sectorTime;
            sectorRecorded[sectorIndex] = true;
            
            // Verificar si es el mejor sector personal y determinar verde en caso correcto, naranja incorrecto
            if (sectorTime < bestSectorTimes[sectorIndex]) 
            {
                bestSectorTimes[sectorIndex] = sectorTime;
                sectorColors[sectorIndex] = Map.SectorColor.GREEN;
            } 
            else 
            {
                sectorColors[sectorIndex] = Map.SectorColor.ORANGE;
            }
        }
        
        //Calcular el tiempo de vuelta al completar una
        public void completeLap() 
        {
            long lapEndTime = timeProvider.now();
            long lastCompletedLapTime = lapEndTime - lapStartTime;
            
            // Actualizar mejor tiempo de vuelta
            if (lastCompletedLapTime < bestLapTime) 
            {
                bestLapTime = lastCompletedLapTime;
            }
        }

        //Hacer el update del Lap Time en caso necesario al pasar por un checkpoint o en update general
        public void updateLapTime() 
        {
            if (lapStartTime > 0) 
            {
                this.lapTime = timeProvider.now() - lapStartTime;
            }
        }
        
        //Verificar que haya pasado por todos los checkpoints
        public boolean passedAllCheckpoints() 
        {
            return passedCheckpoints.size() >= 4; // Asumiendo 4 checkpoints + línea de meta
        }
        
        // Getters
            //Temporizador y tiempo
        public long getLapTime() { return lapTime; }
        public long getBestLapTime() { return bestLapTime == Long.MAX_VALUE ? 0 : bestLapTime; }
        public long getLapStartTime() { return lapStartTime; }
        public long[] getSectorTimes() { return sectorTimes; }

            //Checkpoints
        public Map.SectorColor[] getSectorColors() { return sectorColors; }
        public Set<Integer> getPassedCheckpoints() { return passedCheckpoints; }
        public int getNextCheckpointIndex() { return nextCheckpointIndex; }

            //Carrera o coches
        public int getCurrentLap() { return currentLap; }
        public String getTeam() { return this.team; }
        public double getSkillLevel() { return this.skillLevel; }
        public double getSlipstreamBoost() { return this.slipstreamBoost; }
        public List<Point> getRacingLine() {return this.racingLine;}
        public Integer getCurrentTargetIndex() {return currentTargetIndex;}
        
        //Setters
        public void setNextCheckpointIndex(int index) { this.nextCheckpointIndex = index; }
        public void setTimeProvider(TimeProvider timeProvider) {this.timeProvider = timeProvider;}
        public void setCurrentLap(int lap) {this.currentLap = lap;}
        public void setSectorTime(int index, long time) {sectorTimes[index] = time;}
        public void setPlayerCar(Car playerCar) {this.playerCar = playerCar;}
        public void setCurrentTargetIndex(int index) {this.currentTargetIndex = index;}
        public void setCheckpointPassed(int checkPoint) {this.passedCheckpoints.add(checkPoint);}
        public void setSkillLevel(double skillLevel) 
        { 
            this.skillLevel = skillLevel; 
            setupAIParameters();
        }
        public void setRacingLine(List<Point> racingLine) 
        {
            this.racingLine = new ArrayList<>(racingLine);
            this.currentTargetIndex = 0;
        }
    } 