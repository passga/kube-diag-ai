package com.example.kubediagai.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kubediagai.application.port.in.DiagnosePodUseCase;
import com.example.kubediagai.domain.AiAnalysis;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.DiagnosisResult;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import com.example.kubediagai.domain.Severity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PodDiagnosticControllerTest {

    private final StubDiagnosePodUseCase useCase = new StubDiagnosePodUseCase();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PodDiagnosticController(useCase)).build();
    }

    @Test
    void acceptsPodDiagnosticRequests() throws Exception {
        useCase.result = new DiagnosisResult(
                new PodDiagnosticCommand("argocd", "argocd-server-abc123"),
                List.of(new ClusterFinding(Severity.INFO, "Placeholder", "No Kubernetes client configured")),
                new AiAnalysis("Placeholder analysis", List.of("Wire real adapters")),
                Instant.parse("2026-05-27T07:00:00Z")
        );

        mockMvc.perform(post("/api/pods/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "namespace": "argocd",
                                  "podName": "argocd-server-abc123"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.request.namespace").value("argocd"))
                .andExpect(jsonPath("$.request.podName").value("argocd-server-abc123"))
                .andExpect(jsonPath("$.findings", hasSize(1)))
                .andExpect(jsonPath("$.analysis.summary").value("Placeholder analysis"));

        assertThat(useCase.command)
                .isEqualTo(new PodDiagnosticCommand("argocd", "argocd-server-abc123"));
    }

    @Test
    void rejectsGenericDiagnosticRequests() throws Exception {
        mockMvc.perform(post("/api/diagnostics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "namespace": "default",
                                  "resourceKind": "Pod",
                                  "resourceName": "api-0",
                                  "symptoms": ["not ready"]
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    private static class StubDiagnosePodUseCase implements DiagnosePodUseCase {

        private DiagnosisResult result;
        private PodDiagnosticCommand command;

        @Override
        public DiagnosisResult diagnose(PodDiagnosticCommand command) {
            this.command = command;
            return result;
        }
    }
}
