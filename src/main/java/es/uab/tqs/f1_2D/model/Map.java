package es.uab.tqs.f1_2D.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Map{
    public enum State {IDLE, RUNNING, OFF_TRACK, RESULT, INVALID_LAP}
    public enum SectorColor { NONE, GREEN, ORANGE, PURPLE }
    
    // Datos del mapa
    private final Rectangle finishLine;
    private final List<Rectangle> checkpoints;
    
    // Estado de la carrera
    private State currentState;
    private Set<Integer> passedCheckpoints;
    private int nextCheckpointIndex;
    
    // Tiempos
    private long lapStartTime;
    private long bestLapTime;
    private long lapTime;
    private long lastCompletedLapTime;
    
    // Sectores
    private final int numSectors;
    private long[] sectorTimes;
    private long[] bestSectorTimes;
    private SectorColor[] sectorColors;
    private boolean[] sectorRecorded;
    private long[] lastCompletedSectorTimes;
    private SectorColor[] lastCompletedSectorColors;

    public Map(int mapWidth, int mapHeight, Rectangle finishLine, List<Rectangle> checkpoints) 
    {
        this.finishLine = finishLine;
        this.checkpoints = new ArrayList<>(checkpoints);
        
        this.currentState = State.IDLE;
        this.passedCheckpoints = new HashSet<>();
        this.nextCheckpointIndex = 0;
        this.bestLapTime = Long.MAX_VALUE;
        
        this.numSectors = 3;
        initializeSectors();
    }
    
    private void initializeSectors() 
    {
        this.sectorTimes = new long[numSectors];
        this.bestSectorTimes = new long[numSectors];
        this.sectorColors = new SectorColor[numSectors];
        this.sectorRecorded = new boolean[numSectors];
        this.lastCompletedSectorTimes = new long[numSectors];
        this.lastCompletedSectorColors = new SectorColor[numSectors];
        
        Arrays.fill(bestSectorTimes, Long.MAX_VALUE);
        Arrays.fill(sectorColors, SectorColor.NONE);
        Arrays.fill(sectorRecorded, false);
        Arrays.fill(lastCompletedSectorColors, SectorColor.NONE);
    }
    
    // Getters
    public Rectangle getFinishLine() { return finishLine; }
    public List<Rectangle> getCheckpoints() { return checkpoints; }
    public State getState() { return currentState; }
    public Set<Integer> getPassedCheckpoints() { return passedCheckpoints; }
    public int getNextCheckpointIndex() { return nextCheckpointIndex; }
    public long getLapStartTime() { return lapStartTime; }
    public long getBestLapTime() { return bestLapTime == Long.MAX_VALUE ? 0 : bestLapTime; }
    public long getBestLapTimeFinish() { return bestLapTime; }
    public long getLapTime() { return lapTime; }
    public long getLastCompletedLapTime() { return lastCompletedLapTime; }
    public int getNumSectors() { return numSectors; }
    public long getSectorTime(int i) 
    { 
        if (i < 0 || i >= numSectors) return 0L;
        return sectorTimes[i];
    }
    public long[] getSectorTimes() { return sectorTimes; }
    public long getBestSectorTime(int i) 
    { 
        if(i < 0 || i >= numSectors) return 0L;
        return bestSectorTimes[i]; 
    }
    public long[] getBestSectorTimes() { return bestSectorTimes; }
    public SectorColor getSectorColor(int i) { return sectorColors[i]; }
    public boolean[] getSectorRecorded() { return sectorRecorded; }
        
    // Setters
    public void setCurrentState(State state) { this.currentState = state; }
    public void setNextCheckpointIndex(int index) { this.nextCheckpointIndex = index; }
    public void setLapStartTime(long time) { this.lapStartTime = time; }
    public void setBestLapTime(long time) { this.bestLapTime = time; }
    public void setLapTime(long time) { this.lapTime = time; }
    public void setLastCompletedLapTime(long time) { this.lastCompletedLapTime = time; }    
    public void setSectorTime(int index, long time) {sectorTimes[index] = time;}
    public void setSectorColor(int index, SectorColor color) {sectorColors[index] = color;}
    public void setSectorRecorded(int index, boolean recorded) {sectorRecorded[index] = recorded;}
    public void setBestSectorTime(int index, long time) {bestSectorTimes[index] = time;}
    
    public boolean passedAllCheckpoints() 
    {
        return passedCheckpoints.size() == checkpoints.size() && nextCheckpointIndex == checkpoints.size();
    }
    
    public long sum(long[] array, int index) 
    {
        if (index <= 0) return 0L;
        long suma = 0L;
        for (int i = 0; i < index && i < array.length; i++) suma += array[i];
        return suma;
    }

    
    public void resetSectors() 
    {
        Arrays.fill(sectorTimes, 0L);
        Arrays.fill(sectorRecorded, false);
        Arrays.fill(sectorColors, SectorColor.NONE);
    }
    
    public void copyCurrentToLastSectors() 
    {
        System.arraycopy(sectorTimes, 0, lastCompletedSectorTimes, 0, numSectors);
        System.arraycopy(sectorColors, 0, lastCompletedSectorColors, 0, numSectors);
    }
}