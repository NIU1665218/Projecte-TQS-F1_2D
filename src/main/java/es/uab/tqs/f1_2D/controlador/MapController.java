package es.uab.tqs.f1_2D.controlador;

import es.uab.tqs.f1_2D.model.Map;
import es.uab.tqs.f1_2D.model.TimeProvider;
import javax.swing.Timer;
import java.util.stream.LongStream;

public class MapController {
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
            model.setCurrentState(Map.State.OFF_TRACK);
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
        if (model.getState() == Map.State.OFF_TRACK || model.getState() == Map.State.INVALID_LAP) 
        {
            reset();
            return;
        }
        
        //Si el coche se encuentra en medio de una vuelta, mirar si pasa por chekpoint
        if (model.getState() == Map.State.RUNNING) 
        {
            handleCheckpoints(x, y);
            model.setLapTime(timeProvider.now() - model.getLapStartTime());
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
                    invalidateLap();
                    break;
                }
            }
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
        model.setLastCompletedLapTime(timeProvider.now() - model.getLapStartTime());
        model.copyCurrentToLastSectors();
        model.setCurrentState(Map.State.RESULT);
        
        if (model.getLastCompletedLapTime() < model.getBestLapTimeFinish()) 
        {
            model.setBestLapTime(model.getLastCompletedLapTime());
        }

        if(model.isRaceMode()) 
        { 
            model.incrementLap();
        }
        if(model.getState() != Map.State.RACE_FINISHED)
        {
            if(!isSkipEnabled)
            {
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
                model.setCurrentState(Map.State.IDLE);
                startLap();
                model.setLapStartTime(timeProvider.now() - 3000);
            }
        }
    }
    
    //Si el jugador no sigue el camino correcto del circuito se le invalida la vuelta
    public void invalidateLap() 
    {
        model.setCurrentState(Map.State.INVALID_LAP);
        stopAllTimers();
        offTrackTimer = new Timer(3500, e -> {
            model.setCurrentState(Map.State.IDLE);
            if(model.isRaceMode())
            {
                model.setCurrentState(Map.State.RUNNING);
            }
            offTrackTimer.stop();
        });
        offTrackTimer.setRepeats(false);
        offTrackTimer.start();
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
    private void stopAllTimers() 
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
        if (sectorIndex < 0 || sectorIndex >= model.getNumSectors()) return;
        
        model.setSectorTime(sectorIndex, sectorTime);
        model.setSectorRecorded(sectorIndex, true);
        
        long previousLocalBest = model.getBestSectorTime(sectorIndex);
        long previousGlobalBest = LongStream.of(model.getBestSectorTimes()).min().orElse(Long.MAX_VALUE);
        
        // Mejora local
        if (sectorTime < previousLocalBest) 
        {
            model.setBestSectorTime(sectorIndex, sectorTime);
            model.setSectorColor(sectorIndex, Map.SectorColor.GREEN);
        } else 
        {
            model.setSectorColor(sectorIndex, Map.SectorColor.ORANGE);
        }
        
        long newGlobalBest = LongStream.of(model.getBestSectorTimes()).min().orElse(Long.MAX_VALUE);
        
        // Mejora global real (y existía un global previo)
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

    //Setters/Getters
    public void setMap(Map map) {this.model = map;}
    public Map getModel() { return model;} 
    public Timer getOffTrackTimer() { return offTrackTimer; }
    public Timer getCountdownTimer() { return countdownTimer; }
    public void setSkip(boolean isSkip) { this.isSkipEnabled = isSkip;}
    public boolean getSkip() {return isSkipEnabled;}
}