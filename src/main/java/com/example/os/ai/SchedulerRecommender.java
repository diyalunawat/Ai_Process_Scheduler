package com.example.os.ai;

import com.example.os.model.ComparisonResult;
import java.util.List;

public class SchedulerRecommender {
    public String recommendBest(List<ComparisonResult> results) {
        if (results.isEmpty()) return "None";
        
        ComparisonResult best = results.get(0);
        for (ComparisonResult r : results) {
            if (r.score > best.score) {
                best = r;
            }
        }
        
        return best.algorithmName;
    }
    
    public ComparisonResult getBestResult(List<ComparisonResult> results) {
        if (results.isEmpty()) return null;
        
        ComparisonResult best = results.get(0);
        for (ComparisonResult r : results) {
            if (r.score > best.score) {
                best = r;
            }
        }
        
        return best;
    }
}


