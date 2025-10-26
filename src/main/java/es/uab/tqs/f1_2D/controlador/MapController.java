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
    
    public MapController(Map model) 
    {
        this.model = model;
    }
    
    public void updatePosition(double x, double y, boolean offTrack) 
    {
        if (offTrack) 
        {
            model.setCurrentState(Map.State.OFF_TRACK);
            return;
        }
        
        if (model.getState() == Map.State.RESULT) return;
        
        // Verificar línea de meta
        if (model.getFinishLine().contains(x + 40, y + 40)) 
        {
            handleFinishLineCrossing();
        }
        
        if (model.getState() == Map.State.OFF_TRACK || model.getState() == Map.State.INVALID_LAP) 
        {
            reset();
            return;
        }
        
        if (model.getState() == Map.State.RUNNING) 
        {
            handleCheckpoints(x, y);
            model.setLapTime(timeProvider.now() - model.getLapStartTime());
        }
    }
    
    private void handleFinishLineCrossing() 
    {
        Map.State currentState = model.getState();
        
        if (currentState == Map.State.OFF_TRACK || currentState == Map.State.INVALID_LAP) 
        {
            model.setCurrentState(Map.State.IDLE);
        }
        
        if (currentState == Map.State.IDLE) 
        {
            startLap();
        } else {
            // Grabar último sector si no se ha grabado
            int lastSectorIndex = model.getNumSectors() - 1;
            if (!model.getSectorRecorded()[lastSectorIndex]) 
            {
                long now = timeProvider.now();
                long totalSinceStart = now - model.getLapStartTime();
                long prevSum = model.sum(model.getSectorTimes(), lastSectorIndex);
                long sectorTime = totalSinceStart - prevSum;
                recordSector(lastSectorIndex, sectorTime);
            }
            
            if (model.passedAllCheckpoints()) 
            {
                endLap();
            } else 
            {
                startLap();
            }
        }
    }
    
    private void handleCheckpoints(double x, double y) 
    {
        var checkpoints = model.getCheckpoints();
        for (int i = 0; i < checkpoints.size(); i++) 
        {
            if (checkpoints.get(i).contains(x + 40, y + 40)) 
            {
                if (model.getPassedCheckpoints().contains(i)) break;
                
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
    
    public void startLap() 
    {
        stopAllTimers();
        model.setLapStartTime(timeProvider.now());
        model.resetSectors();
        model.getPassedCheckpoints().clear();
        model.setNextCheckpointIndex(0);
        model.setCurrentState(Map.State.RUNNING);
    }
    
    private void endLap() 
    {
        model.setLastCompletedLapTime(timeProvider.now() - model.getLapStartTime());
        model.copyCurrentToLastSectors();
        model.setCurrentState(Map.State.RESULT);
        
        if (model.getLastCompletedLapTime() < model.getBestLapTimeFinish()) 
        {
            model.setBestLapTime(model.getLastCompletedLapTime());
        }
        
        stopAllTimers();
        resultTimer = new Timer(3000, e -> {
            model.setCurrentState(Map.State.IDLE);
            startLap();
            resultTimer.stop();
        });
        resultTimer.setRepeats(false);
        resultTimer.start();
    }
    
    public void invalidateLap() 
    {
        model.setCurrentState(Map.State.INVALID_LAP);
        stopAllTimers();
        offTrackTimer = new Timer(3500, e -> {
            model.setCurrentState(Map.State.IDLE);
            offTrackTimer.stop();
        });
        offTrackTimer.setRepeats(false);
        offTrackTimer.start();
    }
    
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
    }
    
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
    
    public void setTimeProvider(TimeProvider timeProvider) {this.timeProvider = timeProvider;}
    public void setMap(Map map) {this.model = map;}
    public Map getModel() { return model;} 
    public Timer getOffTrackTimer() { return offTrackTimer; }
}