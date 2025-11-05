package es.uab.tqs.f1_2D.controlador;

import es.uab.tqs.f1_2D.model.Map;
import es.uab.tqs.f1_2D.model.TimeProvider;

import javax.swing.Timer;

import java.util.stream.LongStream;

public class MapController 
{
    private Map model;
    private TimeProvider timeProvider;
    private Timer resultTimer;
    private Timer offTrackTimer;
    private Timer countdownTimer;
    private boolean isSkipEnabled = false; //variable para testing timer
    
    public MapController(Map model) 
    {
        this.model = model;
    }
    
    //Función principal del juego, update de la posición del coche o del countdown
    public void updatePosition(double x, double y, boolean offTrack) 
    {
        //Si está haciendo la cuenta atrás, no procesar
        if (model.isCountdownActive()) 
        {
            updateCountdown();
            return;
        }

         // Si la carrera ha terminado, no procesar
        if (model.getState() == Map.State.RACE_FINISHED) 
        {
            return;
        }

        //Si detecta que está fuera de pista, procesar offtrack
        if (offTrack) 
        {
            handleOffTrack();
            return;
        }
        
        //Si la vuelta ha terminado, no procesar
        if (model.getState() == Map.State.RESULT) return;
        
        // Verificar línea de meta
        if (model.getFinishLine().contains(x + 40, y + 40)) 
        {
            handleFinishLineCrossing();
        }
        
        //Si el coche se encuentra en un estado invalido para hacer tiempo en pista, resetear checkpoints y tiempos
        if (model.getState() == Map.State.INVALID_LAP) 
        {
            if(model.isRaceMode())
            {
                return;
            }
            else
            {
                reset();
                return;
            }
        }
        
        //Si el coche se encuentra en medio de una vuelta, mirar si pasa por chekpoint
        if (model.getState() == Map.State.RUNNING) 
        {
            handleCheckpoints(x, y);
            long baseTime = timeProvider.now() - model.getLapStartTime();
            model.setLapTime(baseTime + model.getCurrentLapPenalty());
        }
    }

    //Función para actualizar el countdown en modo RACE
    private void updateCountdown() 
    {
        long now = timeProvider.now();
        if (now >= model.getCountdownEndTime()) {
            stopAllTimers();
            model.setCurrentState(Map.State.RUNNING);
            model.startRace();
        }
    }
    
    //Si el coche se encuentra encima de la línea de meta, controla su estado
    private void handleFinishLineCrossing() 
    {
        Map.State currentState = model.getState();
        
        //Si viene de hacer una vuelta invalida, resetea su estado
        if (currentState == Map.State.OFF_TRACK || currentState == Map.State.INVALID_LAP) 
        {
            model.setCurrentState(Map.State.IDLE);
        }
        
        //Si su estado es el default, empieza vuelta
        if (currentState == Map.State.IDLE) 
        {
            startLap();
        } 
        else 
        {
           //Si está corriendo mira si es el último checkpoint y cuenta el último sector
            int lastSectorIndex = model.getNumSectors() - 1;
            if (!model.getSectorRecorded()[lastSectorIndex]) 
            {
                long now = timeProvider.now();
                long totalSinceStart = now - model.getLapStartTime();
                long prevSum = model.sum(model.getSectorTimes(), lastSectorIndex);
                long sectorTime = totalSinceStart - prevSum;
                recordSector(lastSectorIndex, sectorTime);
            }
            
            //Si ya ha pasado por todos los checkpoints acaba vuelta, en caso contrario la vuelve a empezar
            if (model.passedAllCheckpoints()) 
            {
                endLap();
            } else 
            {
                startLap();
            }
        }
    }
    
    //Función para comprobar si el coche se encuentra encima de un checkpoint
    private void handleCheckpoints(double x, double y) 
    {
        var checkpoints = model.getCheckpoints();
        //Iterar sobre la lista de checkpoints que contiene el circuito actual
        for (int i = 0; i < checkpoints.size(); i++) 
        {
            if (checkpoints.get(i).contains(x + 40, y + 40)) 
            {
                //Si se encuentra encima de un checkpoint donde ya ha pasado no hace nada
                if (model.getPassedCheckpoints().contains(i)) break;
                
                //Si el checkpoint es el siguiente que debe visitar por orden de circuito, graba sector y tiempo
                //En caso contrario invalida vuelta ya que no está siguiendo el circuito correctamente
                if (i == model.getNextCheckpointIndex()) 
                {
                    model.getPassedCheckpoints().add(i);
                    model.setNextCheckpointIndex(model.getNextCheckpointIndex() + 1);
                    
                    // Grabar sector (excepto para el primer y último checkpoint)
                    if (i >= 1 && i <= checkpoints.size() - 2) 
                    {
                        int sectorIndex = i - 1;
                        long now = timeProvider.now();
                        long totalSinceStart = now - model.getLapStartTime();
                        long prevSum = model.sum(model.getSectorTimes(), sectorIndex);
                        long sectorTime = totalSinceStart - prevSum;
                        recordSector(sectorIndex, sectorTime);
                    }
                } 
                else 
                {
                    handleInvalidLap();
                    break;
                }
            }
        }
    }

    //Función para manejar que se ha ido fuera de pista
    private void handleOffTrack() 
    {
        //Si estamos en carrera procesar castigo, en caso contrario solo poner el estado en offtrack
        if (model.isRaceMode()) 
        {
            //Si la vuelta actual no tiene castigo sumarle 10s
            if (!model.isLapHasPenalty()) 
            {
                model.setCurrentLapPenalty(model.getCurrentLapPenalty() + 10000); 
                model.setLapHasPenalty(true);
            }
            
            //Mostrar el estado OFF_TRACK temporalmente
            model.setCurrentState(Map.State.OFF_TRACK);
            stopAllTimers();
            
            //Saltarse el timer si estamos testeando y volver a running
            if (!isSkipEnabled)
            {
                offTrackTimer = new Timer(2000, e -> {
                    // Después de 2 segundos, volver a RUNNING pero mantener la penalización
                    model.setCurrentState(Map.State.RUNNING);
                    offTrackTimer.stop();
                });
                offTrackTimer.setRepeats(false);
                offTrackTimer.start();
            } 
            else 
            {
                model.setCurrentState(Map.State.RUNNING);
            }
        } 
        else 
        {
            model.setCurrentState(Map.State.OFF_TRACK);
        }
    }
    
    //Función para empezar la vuelta una vez pasado por línea de meta, resetando todos los valores necesarios
    public void startLap() 
    {
        stopAllTimers();
        model.setLapStartTime(timeProvider.now());
        model.resetSectors();
        model.getPassedCheckpoints().clear();
        model.setNextCheckpointIndex(0);
        model.setCurrentState(Map.State.RUNNING);
    }

    //Función para empezar una carrera, visualizando la cuenta atrás
    public void startRaceMode() {
        stopAllTimers();
        model.setRaceMode(true);
        model.startCountdown();
        //Simular que el countdown termina directament pels tests
        if(isSkipEnabled)
        {
            model.startRace();
        }
        else 
        {
            // Timer para actualizar la cuenta atrás
            countdownTimer = new Timer(100, e -> {updateCountdown();});
            countdownTimer.start();
        }
    }
    
    //Función para empezar qualy/practice mode
    public void startQualyMode() 
    {
        stopAllTimers();
        model.setRaceMode(false);
        reset();
    }
    
    //Si se ha pasado por línea de meta y la vuelta se da por finalizada se muestra el resultado o se incrementa vuelta en RACE
    private void endLap() 
    {
        //Calcular el tiempo base de la vuelta
        long baseLapTime = timeProvider.now() - model.getLapStartTime();

        //Copiar los arrays de sectores para pasarlos al overlay
        model.copyCurrentToLastSectors();

        //Cambiar estado a vuelta completada
        model.setCurrentState(Map.State.RESULT);
        
        //Si no tiene castigos y es su mejor vuelta, asignarlo
        if (!model.isLapHasPenalty()) 
        {
            if(baseLapTime < model.getBestLapTimeFinish())
                model.setBestLapTime(baseLapTime);
        }

        //Si estamos en carrera, incrementar vuelta
        if(model.isRaceMode()) 
        { 
            model.incrementLap();
        }

        //Si la carrera no se ha acabado o estamos en modo qualy
        if(model.getState() != Map.State.RACE_FINISHED)
        {
            //Branch para poder testear sin timers
            if(!isSkipEnabled)
            {
                //Enseñar los resultados de la vuelta y preparar la siguiente
                stopAllTimers();
                resultTimer = new Timer(3000, e -> 
                {
                    model.setCurrentState(Map.State.IDLE);
                    startLap();
                    model.setLapStartTime(timeProvider.now() - 3000);
                    resultTimer.stop();
                });
                resultTimer.setRepeats(false);
                resultTimer.start();
            }
            else
            {
                //Empezar siguiente vuelta sin enseñar los resultados, solo para testing
                model.setCurrentState(Map.State.IDLE);
                startLap();
                model.setLapStartTime(timeProvider.now() - 3000);
            }
        }
    }

    //Controlar que el jugador no maneje en sentido contrario
    public void handleInvalidLap() 
    {
        if (model.isRaceMode()) 
        {
            //En modo RACE: sumar 30 segundos de penalización
            if (!model.isLapHasPenalty()) {
                model.setCurrentLapPenalty(model.getCurrentLapPenalty() + 30000); 
                model.setLapHasPenalty(true);
            }
            
            //Mostrar el estado INVALID_LAP temporalmente
            model.setCurrentState(Map.State.INVALID_LAP);
            stopAllTimers();
            
            //Branch para poder testear sin timers
            if (!isSkipEnabled) 
            {
                offTrackTimer = new Timer(2000, e -> {
                    //Después de 2 segundos, volver a RUNNING pero mantener la penalización
                    model.setCurrentState(Map.State.RUNNING);
                    offTrackTimer.stop();
                });
                offTrackTimer.setRepeats(false);
                offTrackTimer.start();
            } 
            else 
            {
                model.setCurrentState(Map.State.RUNNING);
            }
        } 
        else 
        {
            //En QUALY: se invalida el tiempo de la vuelta
            invalidateLap();
        }
    }
    
    //Si el jugador no sigue el camino correcto del circuito se le invalida la vuelta
    public void invalidateLap() 
    {
        // Solo invalidar en modo QUALY
        if (!model.isRaceMode()) {
            model.setCurrentState(Map.State.INVALID_LAP);
            stopAllTimers();
            offTrackTimer = new Timer(3500, e -> {
                model.setCurrentState(Map.State.IDLE);
                offTrackTimer.stop();
            });
            offTrackTimer.setRepeats(false);
            offTrackTimer.start();
        }
    }
    
    //Función de reset de variables de vuelta
    public void reset() 
    {
        stopAllTimers();
        if (model.getState() == Map.State.RUNNING) 
        {
            model.setCurrentState(Map.State.IDLE);
        }
        model.getPassedCheckpoints().clear();
        model.setNextCheckpointIndex(0);
        model.setLapTime(0);
        model.resetSectors();
    }
    
    //Función para parar todos los timers y evitar descontrol de variables sobreescritas
    public void stopAllTimers() 
    {
        if (resultTimer != null && resultTimer.isRunning()) 
        {
            resultTimer.stop();
        }
        if (offTrackTimer != null && offTrackTimer.isRunning()) 
        {
            offTrackTimer.stop();
        }
        if (countdownTimer != null && countdownTimer.isRunning()) 
        {
            countdownTimer.stop();
        }
    }
    
    //Si se detecta que se ha finalizado un sector, se cuenta el tiempo y se determina qué color es dependiendo del tiempo
    public void recordSector(int sectorIndex, long sectorTime) 
    {
        //Comprobar que el sector existe
        if (sectorIndex < 0 || sectorIndex >= model.getNumSectors()) return;
        
        //Asignar el tiempo establecido
        model.setSectorTime(sectorIndex, sectorTime);
        model.setSectorRecorded(sectorIndex, true);
        
        //Obtener los mejores tiempos de sector
        long previousLocalBest = model.getBestSectorTime(sectorIndex);
        long previousGlobalBest = LongStream.of(model.getBestSectorTimes()).min().orElse(Long.MAX_VALUE);
        
        //Si es una mejora local, determinar verde, en caso contrario naranja
        if (sectorTime < previousLocalBest) 
        {
            model.setBestSectorTime(sectorIndex, sectorTime);
            model.setSectorColor(sectorIndex, Map.SectorColor.GREEN);
        } 
        else 
        {
            model.setSectorColor(sectorIndex, Map.SectorColor.ORANGE);
        }
        
        long newGlobalBest = LongStream.of(model.getBestSectorTimes()).min().orElse(Long.MAX_VALUE);
        
        //Si es el mejor tiempo de todos los coches, determinar morado
        if (previousGlobalBest != Long.MAX_VALUE && newGlobalBest < previousGlobalBest) 
        {
            if (model.getBestSectorTime(sectorIndex) == newGlobalBest) 
                model.setSectorColor(sectorIndex, Map.SectorColor.PURPLE);
        }
    }
    
    //Función para pruebas mock
    public void setTimeProvider(TimeProvider timeProvider)
    {
        this.timeProvider = timeProvider;
        this.model.setTimeProvider(timeProvider);
    }

    //Getters
    public Map getModel() { return model;} 
    public Timer getOffTrackTimer() { return offTrackTimer; }
    public Timer getCountdownTimer() { return countdownTimer; }

    //Setters
    public void setMap(Map map) {this.model = map;}
    public void setSkip(boolean isSkip) { this.isSkipEnabled = isSkip;}
}