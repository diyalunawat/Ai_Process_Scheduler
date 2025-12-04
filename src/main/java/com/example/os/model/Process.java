package com.example.os.model;

public class Process {
    public int id;
    public int arrivalTime;
    public int burstTime;
    public int priority;
    public int waitingTime;
    public int turnaroundTime;
    public int completionTime;
    public int remainingTime;
    public int startTime;
    
    public Process(int id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.completionTime = 0;
        this.startTime = -1;
    }
    
    public Process copy() {
        Process p = new Process(id, arrivalTime, burstTime, priority);
        p.waitingTime = waitingTime;
        p.turnaroundTime = turnaroundTime;
        p.completionTime = completionTime;
        p.remainingTime = remainingTime;
        p.startTime = startTime;
        return p;
    }
}

