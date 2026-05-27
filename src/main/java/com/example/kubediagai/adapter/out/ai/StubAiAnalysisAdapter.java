package com.example.kubediagai.adapter.out.ai;

import com.example.kubediagai.application.port.out.AiAnalysisPort;
import com.example.kubediagai.domain.AiAnalysis;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import java.util.List;

public class StubAiAnalysisAdapter implements AiAnalysisPort {

    @Override
    public AiAnalysis analyze(PodDiagnosticCommand command, List<ClusterFinding> findings) {
        return new AiAnalysis(
                "Placeholder analysis for Pod/" + command.podName(),
                List.of("Wire a real AI provider adapter")
        );
    }
}
