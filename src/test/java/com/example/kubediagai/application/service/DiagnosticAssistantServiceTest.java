package com.example.kubediagai.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.application.port.out.AiAnalysisPort;
import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import com.example.kubediagai.domain.AiAnalysis;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.DiagnosisResult;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import com.example.kubediagai.domain.Severity;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiagnosticAssistantServiceTest {

    @Test
    void orchestratesKubernetesFindingsAndAiAnalysis() {
        PodDiagnosticCommand command = new PodDiagnosticCommand(
                "payments",
                "checkout-6d487f5d78-bx2n9"
        );
        ClusterFinding finding = new ClusterFinding(Severity.WARNING, "Restarting pods", "3 restarts in 5 minutes");
        AiAnalysis analysis = new AiAnalysis("Investigate restart loop.", List.of("Check pod logs"));
        Instant now = Instant.parse("2026-05-27T07:00:00Z");

        KubernetesDiagnosticsPort kubernetesPort = actualCommand -> {
            assertThat(actualCommand).isEqualTo(command);
            return List.of(finding);
        };
        AiAnalysisPort aiPort = (actualCommand, actualFindings) -> {
            assertThat(actualCommand).isEqualTo(command);
            assertThat(actualFindings).containsExactly(finding);
            return analysis;
        };
        DiagnosticAssistantService service = new DiagnosticAssistantService(
                kubernetesPort,
                aiPort,
                Clock.fixed(now, ZoneOffset.UTC)
        );

        DiagnosisResult result = service.diagnose(command);

        assertThat(result.request()).isEqualTo(command);
        assertThat(result.findings()).containsExactly(finding);
        assertThat(result.analysis()).isEqualTo(analysis);
        assertThat(result.createdAt()).isEqualTo(now);
    }
}
