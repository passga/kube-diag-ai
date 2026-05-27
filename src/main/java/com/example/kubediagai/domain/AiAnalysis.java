package com.example.kubediagai.domain;

import java.util.List;

public record AiAnalysis(
        String summary,
        List<String> recommendations
) {
    public AiAnalysis {
        recommendations = List.copyOf(recommendations);
    }
}
