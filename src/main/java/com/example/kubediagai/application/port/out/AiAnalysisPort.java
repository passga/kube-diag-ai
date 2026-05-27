package com.example.kubediagai.application.port.out;

import com.example.kubediagai.domain.AiAnalysis;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import java.util.List;

public interface AiAnalysisPort {

    AiAnalysis analyze(PodDiagnosticCommand command, List<ClusterFinding> findings);
}
