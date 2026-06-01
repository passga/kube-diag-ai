package com.example.kubediagai.domain.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import org.junit.jupiter.api.Test;

class PodLogFindingEvaluatorTest {

    private final PodLogFindingEvaluator evaluator = new PodLogFindingEvaluator();

    @Test
    void should_return_info_finding_when_logs_are_empty() {
        ClusterFinding finding = evaluator.evaluate(PodLogDiagnosticState.logsAvailable("app", ""));

        assertThat(finding).satisfies(result -> {
            assertThat(result.severity()).isEqualTo(Severity.INFO);
            assertThat(result.message()).isEqualTo("Recent logs are empty for container app");
            assertThat(result.details()).isEqualTo("Kubernetes returned no recent log lines for container app");
        });
    }

    @Test
    void should_return_info_finding_when_logs_are_blank() {
        ClusterFinding finding = evaluator.evaluate(PodLogDiagnosticState.logsAvailable("app", "   "));

        assertThat(finding).satisfies(result -> {
            assertThat(result.severity()).isEqualTo(Severity.INFO);
            assertThat(result.message()).isEqualTo("Recent logs are empty for container app");
            assertThat(result.details()).isEqualTo("Kubernetes returned no recent log lines for container app");
        });
    }

    @Test
    void should_return_info_finding_when_logs_are_available() {
        ClusterFinding finding = evaluator.evaluate(PodLogDiagnosticState.logsAvailable("app", "first line"));

        assertThat(finding).satisfies(result -> {
            assertThat(result.severity()).isEqualTo(Severity.INFO);
            assertThat(result.message()).isEqualTo("Recent logs for container app");
            assertThat(result.details()).isEqualTo("first line");
        });
    }

    @Test
    void should_strip_logs_when_logs_are_available() {
        ClusterFinding finding = evaluator.evaluate(PodLogDiagnosticState.logsAvailable(
                "app",
                """
                        
                        first line
                        second line
                        
                        """
        ));

        assertThat(finding.details()).isEqualTo("first line\nsecond line");
    }

    @Test
    void should_truncate_logs_when_logs_exceed_max_details_length() {
        ClusterFinding finding = evaluator.evaluate(PodLogDiagnosticState.logsAvailable(
                "app",
                "a".repeat(4_001)
        ));

        assertThat(finding.details()).isEqualTo("a".repeat(4_000) + "...");
    }

    @Test
    void should_return_warning_finding_when_logs_are_unavailable() {
        ClusterFinding finding = evaluator.evaluate(PodLogDiagnosticState.logsUnavailable("app", "failed"));

        assertThat(finding).satisfies(result -> {
            assertThat(result.severity()).isEqualTo(Severity.WARNING);
            assertThat(result.message()).isEqualTo("Recent logs unavailable for container app");
            assertThat(result.details()).isEqualTo("failed");
        });
    }

    @Test
    void should_use_fallback_error_message_when_unavailable_error_message_is_missing() {
        ClusterFinding finding = evaluator.evaluate(PodLogDiagnosticState.logsUnavailable("app", null));

        assertThat(finding.details()).isEqualTo("No error details available");
    }

    @Test
    void should_return_warning_finding_when_pod_has_no_containers() {
        ClusterFinding finding = evaluator.evaluate(PodLogDiagnosticState.noContainers());

        assertThat(finding).satisfies(result -> {
            assertThat(result.severity()).isEqualTo(Severity.WARNING);
            assertThat(result.message()).isEqualTo("Pod has no containers");
            assertThat(result.details()).isEqualTo("No regular or init containers were found in the pod spec");
        });
    }
}
