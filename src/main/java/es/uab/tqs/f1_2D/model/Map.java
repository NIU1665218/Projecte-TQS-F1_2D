package es.uab.tqs.f1_2D.model;

import java.awt.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.sql.Time;
import java.util.*;

public class Map {
    public enum State {IDLE, RUNNING, COMPLETED, OFF_TRACK};
    private int mapHeight;
    private int mapWidth;
    private Rectangle finishLine;
    private List<Rectangle> checkpoints;
    private Set<Integer> passedCheckpoints;
    private long lapStartTime;
    private long bestLapTime;
    private long lapTime;
    private State currentState;
    private TimeProvider timeProvider;

    public Map(int mapWidth, int mapHeight, Rectangle finishLine, List<Rectangle> checkpoints) 
    {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.finishLine = finishLine;
        this.checkpoints = checkpoints;
        this.passedCheckpoints = new HashSet<>();
        this.currentState = State.IDLE;
        this.bestLapTime = Long.MAX_VALUE;
    }

    public void updatePosition(double x, double y, boolean offTrack) 
    {
        if(offTrack) {
            currentState = State.OFF_TRACK;
            return;
        }

        if(currentState == State.OFF_TRACK) { reset(); return;}

        if(finishLine.contains(x,y))
        {
            if(currentState == State.IDLE)
            {
                startLap();
            }
            else if (currentState == State.RUNNING && passedCheckpoints.size() == checkpoints.size())
            {
                endLap();
            }
        }
        if(currentState == State.RUNNING)
        {
            for(int i=0; i<checkpoints.size(); i++)
            {
                if(checkpoints.get(i).contains(x,y))
                {
                    passedCheckpoints.add(i);
                }
            }
        }
        if(currentState == State.RUNNING)
        {
            lapTime = System.currentTimeMillis() - lapStartTime;
        }
    }

        private void startLap() {
        lapStartTime = timeProvider.now();
        passedCheckpoints.clear();
        currentState = State.RUNNING;
    }

    private void endLap() {
        lapTime = timeProvider.now() - lapStartTime;
        currentState = State.COMPLETED;
        if(lapTime < bestLapTime) {
            bestLapTime = lapTime;
        }
    }

    public void reset() {
        if(currentState == State.RUNNING || currentState == State.COMPLETED) 
        {
            currentState = State.IDLE;
        }
        passedCheckpoints.clear();
        lapTime = 0;
    }

    public State getState() { return currentState; }
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

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }
}
