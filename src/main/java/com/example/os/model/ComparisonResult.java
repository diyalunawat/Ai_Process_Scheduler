package com.example.os.model;

public class ComparisonResult {
    public String algorithmName;
    public double avgWaitingTime;
    public double avgTurnaroundTime;
    public int totalTime;
    public double score;
    
    public ComparisonResult(String algorithmName, ScheduleResult result) {
        this.algorithmName = algorithmName;
        this.avgWaitingTime = result.avgWaitingTime;
        this.avgTurnaroundTime = result.avgTurnaroundTime;
        this.totalTime = result.ganttChart.isEmpty() ? 0 : 
                        result.ganttChart.get(result.ganttChart.size() - 1).endTime;
        this.score = calculateScore();
    }
    
    double calculateScore() {
        // Score based on waiting time and turnaround time (lower is better)
        // Normalize to 0-1 range where higher score is better
        double w = 0.5; // weight for waiting time
        double t = 0.5; // weight for turnaround time
        
        double normalizedWaiting = 1.0 / (1.0 + avgWaitingTime);
        double normalizedTurnaround = 1.0 / (1.0 + avgTurnaroundTime);
        
        return w * normalizedWaiting + t * normalizedTurnaround;
    }
    
    public String getAlgorithmName() { return algorithmName; }
    public double getAvgWaitingTime() { return avgWaitingTime; }
    public double getAvgTurnaroundTime() { return avgTurnaroundTime; }
    public double getScore() { return score; }
}

