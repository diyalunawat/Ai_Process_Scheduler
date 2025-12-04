package com.example.os.ai;

import com.example.os.model.Process;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced AI Predictor that uses exponential averaging with adaptive learning
 * and pattern recognition for better burst time prediction.
 */
public class AIPredictor {
    // Prediction storage
    private Map<Integer, Double> predictedBurstTimes = new HashMap<>();
    private Map<Integer, Double> alphaValues = new HashMap<>();
    private Map<Integer, List<Integer>> burstHistory = new HashMap<>();
    
    // Adaptive parameters
    private double defaultAlpha = 0.5;
    private double minAlpha = 0.1;
    private double maxAlpha = 0.9;
    
    // Pattern recognition
    private Map<Integer, Double> varianceMap = new HashMap<>();
    
    /**
     * Updates prediction using exponential averaging with adaptive alpha
     */
    public void updatePrediction(int processId, int actualBurstTime) {
        // Store history
        burstHistory.putIfAbsent(processId, new ArrayList<>());
        List<Integer> history = burstHistory.get(processId);
        history.add(actualBurstTime);
        
        // Keep only recent history (last 10 executions)
        if (history.size() > 10) {
            history.remove(0);
        }
        
        // Calculate variance to determine stability
        double variance = calculateVariance(history);
        varianceMap.put(processId, variance);
        
        // Adaptive alpha: use higher alpha for stable processes, lower for variable
        double alpha = calculateAdaptiveAlpha(processId, variance);
        alphaValues.put(processId, alpha);
        
        // Exponential averaging
        double currentPrediction = predictedBurstTimes.getOrDefault(processId, (double) actualBurstTime);
        double newPrediction = alpha * actualBurstTime + (1 - alpha) * currentPrediction;
        predictedBurstTimes.put(processId, newPrediction);
    }
    
    /**
     * Gets predicted burst time with pattern-based adjustment
     */
    public int getPredictedBurstTime(Process process) {
        if (predictedBurstTimes.containsKey(process.id)) {
            double basePrediction = predictedBurstTimes.get(process.id);
            
            // Apply pattern-based adjustment if we have history
            if (burstHistory.containsKey(process.id) && burstHistory.get(process.id).size() >= 3) {
                double trend = calculateTrend(process.id);
                // Adjust prediction based on trend (up to 10% adjustment)
                basePrediction = basePrediction * (1.0 + trend * 0.1);
            }
            
            return (int) Math.round(Math.max(1, basePrediction));
        }
        
        // Initial prediction: use actual burst time
        return process.burstTime;
    }
    
    /**
     * Optimizes process list by replacing burst times with predictions
     */
    public List<Process> optimizeProcessList(List<Process> processes) {
        List<Process> optimized = new ArrayList<>();
        for (Process p : processes) {
            Process optimizedP = p.copy();
            optimizedP.burstTime = getPredictedBurstTime(p);
            optimized.add(optimizedP);
        }
        return optimized;
    }
    
    /**
     * Calculates adaptive alpha based on process variance
     * Stable processes (low variance) get lower alpha (more weight to history)
     * Variable processes (high variance) get higher alpha (more weight to recent)
     */
    private double calculateAdaptiveAlpha(int processId, double variance) {
        if (variance < 1.0) {
            // Very stable process - trust history more
            return minAlpha + 0.1;
        } else if (variance > 10.0) {
            // Very variable process - trust recent more
            return maxAlpha - 0.1;
        } else {
            // Moderate variance - use default with slight adjustment
            double normalizedVariance = Math.min(1.0, variance / 10.0);
            return defaultAlpha + (normalizedVariance * 0.2);
        }
    }
    
    /**
     * Calculates variance of burst time history
     */
    private double calculateVariance(List<Integer> history) {
        if (history.size() < 2) return 0.0;
        
        double mean = history.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = history.stream()
            .mapToDouble(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);
        
        return variance;
    }
    
    /**
     * Calculates trend in burst times (positive = increasing, negative = decreasing)
     */
    private double calculateTrend(int processId) {
        List<Integer> history = burstHistory.get(processId);
        if (history == null || history.size() < 3) return 0.0;
        
        // Simple linear trend: compare recent average to older average
        int recentCount = Math.min(3, history.size());
        int olderCount = Math.min(3, history.size() - recentCount);
        
        if (olderCount == 0) return 0.0;
        
        double recentAvg = history.subList(history.size() - recentCount, history.size())
            .stream().mapToInt(Integer::intValue).average().orElse(0.0);
        
        double olderAvg = history.subList(0, olderCount)
            .stream().mapToInt(Integer::intValue).average().orElse(0.0);
        
        // Normalize trend to -1 to 1 range
        if (olderAvg == 0) return 0.0;
        return (recentAvg - olderAvg) / olderAvg;
    }
    
    /**
     * Resets all predictions and history
     */
    public void reset() {
        predictedBurstTimes.clear();
        alphaValues.clear();
        burstHistory.clear();
        varianceMap.clear();
    }
    
    /**
     * Gets current prediction for a process (for debugging/display)
     */
    public double getCurrentPrediction(int processId) {
        return predictedBurstTimes.getOrDefault(processId, 0.0);
    }
}


