package com.example.os.scheduler;

import com.example.os.model.Process;
import com.example.os.model.ScheduleResult;
import com.example.os.model.GanttEntry;
import java.util.List;

public abstract class Scheduler {
    public abstract ScheduleResult schedule(List<Process> processes);
    
    protected ScheduleResult calculateMetrics(List<Process> processes, List<GanttEntry> ganttChart) {
        double totalWaiting = 0;
        double totalTurnaround = 0;
        
        for (Process p : processes) {
            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
        }
        
        double avgWaiting = processes.isEmpty() ? 0 : totalWaiting / processes.size();
        double avgTurnaround = processes.isEmpty() ? 0 : totalTurnaround / processes.size();
        
        return new ScheduleResult(processes, ganttChart, avgWaiting, avgTurnaround);
    }
}

