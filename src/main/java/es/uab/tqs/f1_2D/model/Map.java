package es.uab.tqs.f1_2D.model;

import java.awt.*;
import java.util.List;
import java.util.stream.LongStream;
import java.util.*;

public class Map {
    public enum State {IDLE, RUNNING, OFF_TRACK, RESULT, INVALID_LAP};
    private int mapHeight;
    private int mapWidth;

    private Rectangle finishLine;
    private List<Rectangle> checkpoints;
    private Set<Integer> passedCheckpoints;
    private int nextCheckpointIndex = 0;

    private long lapStartTime;
    private long bestLapTime;
    private long lapTime;
    private long lastCompletedLapTime; 
    private Timer resultTimer;
    private Timer offTrackTimer;

    private State currentState;
    private TimeProvider timeProvider;

    public enum SectorColor { NONE, GREEN, ORANGE, PURPLE }
    private int numSectors;
    private long[] sectorTimes;       
    private long[] bestSectorTimes;  
    private SectorColor[] sectorColors;
    private boolean[] sectorRecorded; 
    private long[] lastCompletedSectorTimes;
    private SectorColor[] lastCompletedSectorColors;

    public Map(int mapWidth, int mapHeight, Rectangle finishLine, List<Rectangle> checkpoints) 
    {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.finishLine = finishLine;
        this.checkpoints = new ArrayList<>(checkpoints);
        this.passedCheckpoints = new HashSet<>();
        this.currentState = State.IDLE;
        this.bestLapTime = Long.MAX_VALUE;

        this.numSectors = 3;
        this.sectorTimes = new long[numSectors];
        this.bestSectorTimes = new long[numSectors];
        this.sectorColors = new SectorColor[numSectors];
        this.sectorRecorded = new boolean[numSectors];
        this.nextCheckpointIndex = 0;

        this.lastCompletedSectorTimes = new long[numSectors];
        this.lastCompletedSectorColors = new SectorColor[numSectors];

        Arrays.fill(bestSectorTimes, Long.MAX_VALUE);
        Arrays.fill(sectorColors, SectorColor.NONE);
        Arrays.fill(sectorRecorded, false);
        Arrays.fill(lastCompletedSectorColors, SectorColor.NONE);
    }

    public void updatePosition(double x, double y, boolean offTrack) 
    {
        if(offTrack) {
            currentState = State.OFF_TRACK;
            return;
        }

        if(currentState == State.RESULT) return;

        if(finishLine.contains(x + 40,y + 40))
        {
            if(currentState == State.OFF_TRACK || currentState == State.INVALID_LAP) currentState = State.IDLE;
            if(currentState == State.IDLE || currentState == State.INVALID_LAP)
            {
                startLap();
            }
            else if (currentState == State.RUNNING)
            {
                int lastSectorIndex = numSectors - 1;
                if(!sectorRecorded[lastSectorIndex]) 
                {
                    long now = timeProvider.now();
                    long totalSinceStart = now - lapStartTime;
                    long prevSum = sum(sectorTimes, lastSectorIndex);
                    long sectorTime = totalSinceStart - prevSum;
                    recordSector(lastSectorIndex, sectorTime);
                }

                if(passedAllCheckpoints())
                {
                    endLap();
                }
                else
                {
                    startLap();
                }
            }
        }
        if(currentState == State.OFF_TRACK || currentState == State.INVALID_LAP) { reset(); return;}
        if(currentState == State.RUNNING)
        {
            for(int i=0; i<checkpoints.size(); i++)
            {
                if(checkpoints.get(i).contains(x + 40,y + 40))
                {
                    if(passedCheckpoints.contains(i)) break;
                    if (i == nextCheckpointIndex)
                    {
                       passedCheckpoints.add(i);  
                       nextCheckpointIndex++; 
                    

                        if (i >= 1 && i <= checkpoints.size() - 2) 
                        {
                            int sectorIndex = i - 1;
                            if (sectorIndex >= 0 && sectorIndex < numSectors - 1) {
                                
                                if (!sectorRecorded[sectorIndex]) {
                                    long now = timeProvider.now();
                                    long totalSinceStart = now - lapStartTime;
                                    long prevSum = sum(sectorTimes, sectorIndex);
                                    long sectorTime = totalSinceStart - prevSum;
                                    recordSector(sectorIndex, sectorTime);
                                }
                            }
                        }
                    }
                    else
                    {
                        invalidateLap();
                        break;
                    }
                }
            }

            lapTime = timeProvider.now() - lapStartTime;
        }
    }

    private void startLap() {
        lapStartTime = timeProvider.now();
        Arrays.fill(sectorTimes, 0L);
        Arrays.fill(sectorRecorded, false);
        Arrays.fill(sectorColors, SectorColor.NONE);
        passedCheckpoints.clear();
        nextCheckpointIndex = 0;
        currentState = State.RUNNING;
    }

    private void endLap() {
        lastCompletedLapTime = timeProvider.now() - lapStartTime;

        System.arraycopy(sectorTimes, 0, lastCompletedSectorTimes, 0, numSectors);
        System.arraycopy(sectorColors, 0, lastCompletedSectorColors, 0, numSectors);
        
        currentState = State.RESULT;
        
        if(lastCompletedLapTime < bestLapTime) {
            bestLapTime = lastCompletedLapTime;
        }
    
        if (resultTimer != null) {
            resultTimer.cancel();
        }
        resultTimer = new Timer();
        resultTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                currentState = State.IDLE;
                startLap();
            }
        }, 3000);
    }

    private void invalidateLap() {
        currentState = State.INVALID_LAP;
        
        if (offTrackTimer != null) {
            offTrackTimer.cancel();
        }
        offTrackTimer = new Timer();
        offTrackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                currentState = State.IDLE;
            }
        }, 5000);
    }

    public void reset() {
        if(currentState == State.RUNNING) 
        {
            currentState = State.IDLE;
        }
        passedCheckpoints.clear();
        nextCheckpointIndex = 0;
        lapTime = 0;
        Arrays.fill(sectorTimes, 0L);
        Arrays.fill(sectorRecorded, false);
        Arrays.fill(sectorColors, SectorColor.NONE);
    }

    
    public void updateSectorTime(int sectorIndex, long time) 
    {
        sectorTimes[sectorIndex] = time;

        if(time < bestSectorTimes[sectorIndex]) 
        {
            bestSectorTimes[sectorIndex] = time;
            sectorColors[sectorIndex] = SectorColor.GREEN;
        } 
        else 
        {
            sectorColors[sectorIndex] = SectorColor.ORANGE;
        }

      
        long globalBest = Arrays.stream(bestSectorTimes).min().orElse(Long.MAX_VALUE);
        if(time <= globalBest) 
        {
            sectorColors[sectorIndex] = SectorColor.PURPLE;
        }
    }

    private void recordSector(int sectorIndex, long sectorTime) 
    {
        if (sectorIndex < 0 || sectorIndex >= numSectors) return;
        sectorTimes[sectorIndex] = sectorTime;
        sectorRecorded[sectorIndex] = true;

        if (sectorTime < bestSectorTimes[sectorIndex]) {
            bestSectorTimes[sectorIndex] = sectorTime;
            sectorColors[sectorIndex] = SectorColor.GREEN;
        } else {
            sectorColors[sectorIndex] = SectorColor.ORANGE;
        }

        long globalBest = LongStream.of(bestSectorTimes).min().orElse(Long.MAX_VALUE);
        if (sectorTime <= globalBest && globalBest != Long.MAX_VALUE) {
            sectorColors[sectorIndex] = SectorColor.PURPLE;
            bestSectorTimes[sectorIndex] = sectorTime;
        }
    }

    private boolean passedAllCheckpoints() 
    {
        return passedCheckpoints.size() == checkpoints.size() && nextCheckpointIndex == checkpoints.size();
    }

    public State getState() { return currentState; }
    public void setState(State state) { this.currentState = state; }
    public long getLapTime() { return lapTime; }
    public long getBestLapTime() { return bestLapTime == Long.MAX_VALUE ? 0 : bestLapTime; }
    public Set<Integer> getPassedCheckpoints() { return passedCheckpoints; }

    public int getMapHeight()
    {
        return mapHeight;
    }
    public int getMapWidth()
    {
        return mapWidth;
    }

    public SectorColor getSectorColor(int index)
    {
        return sectorColors[index];
    }

    public long getSectorTime(int index) {
        if (index < 0 || index >= numSectors) return 0L;
        return sectorTimes[index];
    }

    public long getBestSectorTime(int index) {
        if (index < 0 || index >= numSectors) return 0L;
        return bestSectorTimes[index] == Long.MAX_VALUE ? 0L : bestSectorTimes[index];
    }

    public int getNumSectors() {
        return numSectors;
    }


    private long sum(long[] array, int index) {
        if (index <= 0) return 0L;
        long suma = 0L;
        for (int i = 0; i < index && i < array.length; i++) suma += array[i];
        return suma;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }
}
