package com.example.os.scheduler;

import com.example.os.model.Process;
import com.example.os.model.ScheduleResult;
import com.example.os.model.GanttEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FCFSScheduler extends Scheduler {
    @Override
    public ScheduleResult schedule(List<Process> processes) {
        List<Process> sorted = new ArrayList<>();
        for (Process p : processes) {
            sorted.add(p.copy());
        }
        sorted.sort(Comparator.comparingInt(p -> p.arrivalTime));
        
        List<GanttEntry> gantt = new ArrayList<>();
        int currentTime = 0;
        
        for (Process p : sorted) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            p.startTime = currentTime;
            p.waitingTime = Math.max(0, currentTime - p.arrivalTime); // Ensure non-negative
            p.completionTime = currentTime + p.burstTime;
            p.turnaroundTime = Math.max(0, p.completionTime - p.arrivalTime); // Ensure non-negative
            gantt.add(new GanttEntry(p.id, currentTime, p.completionTime));
            currentTime = p.completionTime;
        }
        
        return calculateMetrics(sorted, gantt);
    }
}


