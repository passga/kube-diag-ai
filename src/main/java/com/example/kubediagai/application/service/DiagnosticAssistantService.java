package com.example.kubediagai.application.service;

import com.example.kubediagai.application.port.in.DiagnosePodUseCase;
import com.example.kubediagai.application.port.out.AiAnalysisPort;
import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import com.example.kubediagai.domain.AiAnalysis;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.DiagnosisResult;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class DiagnosticAssistantService implements DiagnosePodUseCase {

    private final KubernetesDiagnosticsPort kubernetesDiagnosticsPort;
    private final AiAnalysisPort aiAnalysisPort;
    private final Clock clock;

    public DiagnosticAssistantService(
            KubernetesDiagnosticsPort kubernetesDiagnosticsPort,
            AiAnalysisPort aiAnalysisPort
    ) {
        this(kubernetesDiagnosticsPort, aiAnalysisPort, Clock.systemUTC());
    }

    DiagnosticAssistantService(
            KubernetesDiagnosticsPort kubernetesDiagnosticsPort,
            AiAnalysisPort aiAnalysisPort,
            Clock clock
    ) {
        this.kubernetesDiagnosticsPort = kubernetesDiagnosticsPort;
        this.aiAnalysisPort = aiAnalysisPort;
        this.clock = clock;
    }

    @Override
    public DiagnosisResult diagnose(PodDiagnosticCommand command) {
        List<ClusterFinding> findings = kubernetesDiagnosticsPort.collectPodFindings(command);
        AiAnalysis analysis = aiAnalysisPort.analyze(command, findings);

        return new DiagnosisResult(command, findings, analysis, Instant.now(clock));
    }
}
