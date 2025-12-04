package com.example.os.scheduler;

import com.example.os.ai.AIPredictor;
import com.example.os.model.Process;
import com.example.os.model.ScheduleResult;
import com.example.os.model.GanttEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Intelligent AI Scheduler that uses multi-factor decision making
 * to optimize process scheduling based on learned patterns and predictions.
 */
public class IntelligentAIScheduler extends Scheduler {
    private AIPredictor predictor;
    private double learningRate = 0.3;
    
    // Historical performance data
    private List<Double> historicalWaitingTimes = new ArrayList<>();
    private List<Double> historicalTurnaroundTimes = new ArrayList<>();
    
    public IntelligentAIScheduler(AIPredictor predictor) {
        this.predictor = predictor;
    }
    
    @Override
    public ScheduleResult schedule(List<Process> processes) {
        if (processes.isEmpty()) {
            return new ScheduleResult(new ArrayList<>(), new ArrayList<>(), 0, 0);
        }
        
        // Create copies to avoid modifying originals
        List<Process> workingProcesses = new ArrayList<>();
        Map<Integer, Integer> originalBurstTimes = new java.util.HashMap<>();
        for (Process p : processes) {
            workingProcesses.add(p.copy());
            originalBurstTimes.put(p.id, p.burstTime); // Store original burst time
        }
        
        // Use AI to predict and optimize burst times
        List<Process> optimized = predictor.optimizeProcessList(workingProcesses);
        
        // Intelligent scheduling using multi-factor scoring
        List<GanttEntry> gantt = new ArrayList<>();
        int currentTime = 0;
        List<Process> ready = new ArrayList<>();
        List<Process> completed = new ArrayList<>();
        int idx = 0;
        
        // Sort by arrival time first
        optimized.sort(Comparator.comparingInt(p -> p.arrivalTime));
        
        while (idx < optimized.size() || !ready.isEmpty()) {
            // Add all arrived processes to ready queue
            while (idx < optimized.size() && optimized.get(idx).arrivalTime <= currentTime) {
                ready.add(optimized.get(idx++));
            }
            
            if (ready.isEmpty()) {
                if (idx < optimized.size()) {
                    currentTime = optimized.get(idx).arrivalTime;
                    continue;
                }
                break;
            }
            
            // AI-based process selection using multi-factor scoring
            Process selected = selectBestProcess(ready, currentTime);
            ready.remove(selected);
            
            if (selected.startTime == -1) {
                selected.startTime = currentTime;
            }
            
            // Calculate execution time (could be partial for preemption)
            int executionTime = selected.remainingTime;
            
            // Check if we should preempt (for better overall performance)
            if (ready.size() > 0 && shouldPreempt(selected, ready, currentTime)) {
                // Execute for a time quantum and then reconsider
                int quantum = calculateAdaptiveQuantum(selected, ready);
                executionTime = Math.min(quantum, selected.remainingTime);
            }
            
            gantt.add(new GanttEntry(selected.id, currentTime, currentTime + executionTime));
            currentTime += executionTime;
            selected.remainingTime -= executionTime;
            
            // Add any newly arrived processes to ready queue
            while (idx < optimized.size() && optimized.get(idx).arrivalTime <= currentTime) {
                ready.add(optimized.get(idx++));
            }
            
            if (selected.remainingTime <= 0) {
                selected.completionTime = currentTime;
                selected.turnaroundTime = Math.max(0, selected.completionTime - selected.arrivalTime); // Ensure non-negative
                // Use original burst time for waiting time calculation
                int originalBurstTime = originalBurstTimes.getOrDefault(selected.id, selected.burstTime);
                selected.waitingTime = Math.max(0, selected.turnaroundTime - originalBurstTime); // Ensure non-negative
                completed.add(selected);
                
                // Update AI predictions using original burst time
                predictor.updatePrediction(selected.id, originalBurstTime);
            } else {
                // Process not completed, add back to ready queue
                ready.add(selected);
            }
        }
        
        // Ensure all processes are in completed list (they should be, but safety check)
        // Update original processes with results from completed list
        for (Process original : processes) {
            Process completedProc = findProcessById(completed, original.id);
            if (completedProc != null) {
                original.waitingTime = completedProc.waitingTime;
                original.turnaroundTime = completedProc.turnaroundTime;
                original.completionTime = completedProc.completionTime;
                original.startTime = completedProc.startTime;
            } else {
                // Safety: if process not found in completed, it means it wasn't scheduled
                // This shouldn't happen, but handle it gracefully
                System.err.println("Warning: Process " + original.id + " not found in completed list");
                // Set default values to avoid division issues
                original.waitingTime = 0;
                original.turnaroundTime = 0;
            }
        }
        
        // Calculate metrics on the updated processes list
        ScheduleResult result = calculateMetrics(processes, gantt);
        
        // Learn from this execution
        learnFromExecution(result);
        
        return result;
    }
    
    /**
     * Selects the best process using AI-based multi-factor scoring
     */
    private Process selectBestProcess(List<Process> ready, int currentTime) {
        if (ready.size() == 1) {
            return ready.get(0);
        }
        
        Process best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Process p : ready) {
            double score = calculateProcessScore(p, currentTime, ready);
            if (score > bestScore) {
                bestScore = score;
                best = p;
            }
        }
        
        return best;
    }
    
    /**
     * Calculates a multi-factor score for process selection
     * Factors: predicted burst time, priority, waiting time, urgency
     */
    private double calculateProcessScore(Process p, int currentTime, List<Process> readyQueue) {
        double score = 0.0;
        
        // Factor 1: Priority (higher priority = higher score)
        // Normalize priority (lower number = higher priority, so invert)
        double priorityScore = 1.0 / (1.0 + p.priority);
        score += priorityScore * 0.25;
        
        // Factor 2: Predicted burst time (shorter = better, but not always)
        int predictedBurst = predictor.getPredictedBurstTime(p);
        double burstScore = 1.0 / (1.0 + predictedBurst);
        score += burstScore * 0.30;
        
        // Factor 3: Waiting time (longer waiting = higher urgency)
        int waitingTime = currentTime - p.arrivalTime;
        double waitingScore = Math.log(1.0 + waitingTime) / 10.0; // Logarithmic to prevent dominance
        score += waitingScore * 0.25;
        
        // Factor 4: Urgency (how critical is it to run now)
        double urgency = calculateUrgency(p, currentTime, readyQueue);
        score += urgency * 0.20;
        
        return score;
    }
    
    /**
     * Calculates urgency based on process characteristics and queue state
     */
    private double calculateUrgency(Process p, int currentTime, List<Process> readyQueue) {
        double urgency = 0.0;
        
        // If process has been waiting long, increase urgency
        int waitTime = currentTime - p.arrivalTime;
        if (waitTime > p.burstTime) {
            urgency += 0.5; // High urgency if waiting longer than execution time
        }
        
        // If process is short and many are waiting, it's urgent to clear it
        if (p.burstTime <= 3 && readyQueue.size() > 2) {
            urgency += 0.3;
        }
        
        // High priority processes are more urgent
        if (p.priority <= 2) {
            urgency += 0.2;
        }
        
        return Math.min(1.0, urgency);
    }
    
    /**
     * Determines if current process should be preempted
     */
    private boolean shouldPreempt(Process current, List<Process> ready, int currentTime) {
        if (ready.isEmpty()) return false;
        
        // Find best alternative process
        Process bestAlternative = selectBestProcess(ready, currentTime);
        
        // Preempt if alternative is significantly better
        double currentScore = calculateProcessScore(current, currentTime, ready);
        double alternativeScore = calculateProcessScore(bestAlternative, currentTime, ready);
        
        // Preempt if alternative is 20% better and current has run for a while
        if (alternativeScore > currentScore * 1.2) {
            int runTime = currentTime - current.startTime;
            if (runTime > 2) { // Don't preempt immediately
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calculates adaptive time quantum based on process characteristics
     */
    private int calculateAdaptiveQuantum(Process p, List<Process> readyQueue) {
        int baseQuantum = 2;
        
        // Adjust quantum based on predicted burst time
        int predictedBurst = predictor.getPredictedBurstTime(p);
        if (predictedBurst < 5) {
            baseQuantum = Math.min(predictedBurst, 3);
        } else if (predictedBurst > 10) {
            baseQuantum = 4;
        }
        
        // Adjust based on queue length (shorter quantum if many waiting)
        if (readyQueue.size() > 3) {
            baseQuantum = Math.max(1, baseQuantum - 1);
        }
        
        return baseQuantum;
    }
    
    /**
     * Learns from execution results to improve future decisions
     */
    private void learnFromExecution(ScheduleResult result) {
        historicalWaitingTimes.add(result.avgWaitingTime);
        historicalTurnaroundTimes.add(result.avgTurnaroundTime);
        
        // Keep only recent history (last 10 executions)
        if (historicalWaitingTimes.size() > 10) {
            historicalWaitingTimes.remove(0);
            historicalTurnaroundTimes.remove(0);
        }
        
        // Adjust learning rate based on performance trends
        if (historicalWaitingTimes.size() >= 3) {
            double recentAvg = historicalWaitingTimes.subList(
                historicalWaitingTimes.size() - 3, 
                historicalWaitingTimes.size()
            ).stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            double olderAvg = historicalWaitingTimes.subList(
                0, 
                Math.min(3, historicalWaitingTimes.size() - 3)
            ).stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            // If performance is improving, increase learning rate slightly
            if (recentAvg < olderAvg) {
                learningRate = Math.min(0.5, learningRate * 1.05);
            } else {
                learningRate = Math.max(0.1, learningRate * 0.95);
            }
        }
    }
    
    private Process findProcessById(List<Process> processes, int id) {
        for (Process p : processes) {
            if (p.id == id) {
                return p;
            }
        }
        return null;
    }
}

