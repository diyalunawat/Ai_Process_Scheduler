package com.example.os.model;

public class GanttEntry {
    public int processId;
    public int startTime;
    public int endTime;
    
    public GanttEntry(int processId, int startTime, int endTime) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}

