package com.example.os.scheduler;

import com.example.os.model.Process;
import com.example.os.model.ScheduleResult;
import com.example.os.model.GanttEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobinScheduler extends Scheduler {
    int timeQuantum = 2;
    
    public RoundRobinScheduler(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }
    
    @Override
    public ScheduleResult schedule(List<Process> processes) {
        List<Process> sorted = new ArrayList<>();
        for (Process p : processes) {
            sorted.add(p.copy());
        }
        sorted.sort(Comparator.comparingInt(p -> p.arrivalTime));
        
        List<GanttEntry> gantt = new ArrayList<>();
        Queue<Process> ready = new LinkedList<>();
        int currentTime = 0;
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
            
            Process p = ready.poll();
            if (p.startTime == -1) {
                p.startTime = currentTime;
            }
            
            int execTime = Math.min(timeQuantum, p.remainingTime);
            gantt.add(new GanttEntry(p.id, currentTime, currentTime + execTime));
            currentTime += execTime;
            p.remainingTime -= execTime;
            
            while (idx < sorted.size() && sorted.get(idx).arrivalTime <= currentTime) {
                ready.add(sorted.get(idx++));
            }
            
            if (p.remainingTime > 0) {
                ready.add(p);
            } else {
                p.completionTime = currentTime;
                p.turnaroundTime = Math.max(0, p.completionTime - p.arrivalTime); // Ensure non-negative
                p.waitingTime = Math.max(0, p.turnaroundTime - p.burstTime); // Ensure non-negative
            }
        }
        
        return calculateMetrics(sorted, gantt);
    }
}


