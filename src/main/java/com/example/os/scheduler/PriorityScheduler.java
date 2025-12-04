package com.example.os.scheduler;

import com.example.os.model.Process;
import com.example.os.model.ScheduleResult;
import com.example.os.model.GanttEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PriorityScheduler extends Scheduler {
    @Override
    public ScheduleResult schedule(List<Process> processes) {
        List<Process> sorted = new ArrayList<>();
        for (Process p : processes) {
            sorted.add(p.copy());
        }
        sorted.sort(Comparator.<Process>comparingInt(p -> p.arrivalTime).thenComparingInt(p -> p.priority));
        
        List<GanttEntry> gantt = new ArrayList<>();
        int currentTime = 0;
        List<Process> ready = new ArrayList<>();
        int idx = 0;
        
        while (idx < sorted.size() || !ready.isEmpty()) {
            while (idx < sorted.size() && sorted.get(idx).arrivalTime <= currentTime) {
                ready.add(sorted.get(idx++));
            }
            
            if (ready.isEmpty()) {
                if (idx < sorted.size()) {
                    currentTime = sorted.get(idx).arrivalTime;
                    continue;
                }
                break;
            }
            
            ready.sort(Comparator.comparingInt(p -> p.priority));
            Process p = ready.remove(0);
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

