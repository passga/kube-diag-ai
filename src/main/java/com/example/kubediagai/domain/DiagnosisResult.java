package com.example.kubediagai.domain;

import java.time.Instant;
import java.util.List;

public record DiagnosisResult(
        PodDiagnosticCommand request,
        List<ClusterFinding> findings,
        AiAnalysis analysis,
        Instant createdAt
) {
    public DiagnosisResult {
        findings = List.copyOf(findings);
    }
}
