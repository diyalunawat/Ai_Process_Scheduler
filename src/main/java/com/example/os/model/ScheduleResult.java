package com.example.os.model;

import java.util.List;

public class ScheduleResult {
    public List<Process> processes;
    public List<GanttEntry> ganttChart;
    public double avgWaitingTime;
    public double avgTurnaroundTime;
    
    public ScheduleResult(List<Process> processes, List<GanttEntry> ganttChart, 
                         double avgWaitingTime, double avgTurnaroundTime) {
        this.processes = processes;
        this.ganttChart = ganttChart;
        this.avgWaitingTime = avgWaitingTime;
        this.avgTurnaroundTime = avgTurnaroundTime;
    }
}

