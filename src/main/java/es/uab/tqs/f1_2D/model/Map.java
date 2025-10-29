package es.uab.tqs.f1_2D.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Map{
    public enum State {IDLE, RUNNING, OFF_TRACK, RESULT, INVALID_LAP, COUNTDOWN, RACE_FINISHED}
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
    private TimeProvider timeProvider;
    
    // Sectores
    private final int numSectors;
    private long[] sectorTimes;
    private long[] bestSectorTimes;
    private SectorColor[] sectorColors;
    private boolean[] sectorRecorded;
    private long[] lastCompletedSectorTimes;
    private SectorColor[] lastCompletedSectorColors;

    //Carrera
    private boolean raceMode = false;
    private int totalLaps = 3; 
    private int currentLap = 0;
    private long raceStartTime;
    private long countdownEndTime;
    private int countdownSeconds = 5;

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
    public boolean isRaceMode() { return raceMode; }
    public int getTotalLaps() { return totalLaps; }   
    public int getCurrentLap() { return currentLap; }   
    public long getRaceStartTime() { return raceStartTime; }    
    public long getCountdownEndTime() { return countdownEndTime; }    
    public int getCountdownSeconds() { return countdownSeconds; }
    public boolean isRaceComplete() { return raceMode && currentLap > totalLaps;}  
    public boolean isCountdownActive() { return currentState == State.COUNTDOWN; }
    public int getRemainingCountdown() 
    {
        if (!isCountdownActive()) return 0;
        long now = timeProvider.now();
        long remaining = countdownEndTime - now;
        return (int) Math.max(0, (remaining / 1000) + 1);
    }
        
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
    public void setRaceMode(boolean raceMode) { this.raceMode = raceMode; }
    public void setTotalLaps(int totalLaps) { this.totalLaps = totalLaps; }
    public void setCurrentLap(int currentLap) { this.currentLap = currentLap; }
    public void setRaceStartTime(long raceStartTime) { this.raceStartTime = raceStartTime; }
    public void setCountdownEndTime(long countdownEndTime) { this.countdownEndTime = countdownEndTime; }
    public void setCountdownSeconds(int countdownSeconds) { this.countdownSeconds = countdownSeconds; }
    public void setTimeProvider(TimeProvider timeProvider) {this.timeProvider = timeProvider;}
    
    //Comprobación si se ha pasado por todos los checkpoints que contiene el circuito
    public boolean passedAllCheckpoints() 
    {
        return passedCheckpoints.size() == checkpoints.size() && nextCheckpointIndex == checkpoints.size();
    }
    
    //Función que suma los tiempos de los sectores 
    public long sum(long[] array, int index) 
    {
        if (index <= 0) return 0L;
        long suma = 0L;
        for (int i = 0; i < index && i < array.length; i++) suma += array[i];
        return suma;
    }

    //Reset de los sectores para volver a empezar
    public void resetSectors() 
    {
        Arrays.fill(sectorTimes, 0L);
        Arrays.fill(sectorRecorded, false);
        Arrays.fill(sectorColors, SectorColor.NONE);
    }
    
    //Función para copiar los arrays al momento de visualizar los resultados de una vuelta
    public void copyCurrentToLastSectors() 
    {
        System.arraycopy(sectorTimes, 0, lastCompletedSectorTimes, 0, numSectors);
        System.arraycopy(sectorColors, 0, lastCompletedSectorColors, 0, numSectors);
    }

    //Iniciar cuenta atrás
    public void startCountdown() {
        currentState = State.COUNTDOWN;
        countdownEndTime = timeProvider.now() + (countdownSeconds * 1000);
    }
    
    // Finalizar cuenta atrás e iniciar carrera
    public void startRace() {
        currentState = State.RUNNING;
        raceStartTime = timeProvider.now();
        lapStartTime = raceStartTime;
        currentLap = 1; 
    }
    
    // Incrementar vuelta
    public void incrementLap() {
        currentLap++;
        lapStartTime = timeProvider.now(); 
        resetSectors();                    
        passedCheckpoints.clear();         
        nextCheckpointIndex = 0;
        if (isRaceComplete()) {
            currentState = State.RACE_FINISHED;
        } 
    }
}