package com.example.kubediagai.domain.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.List;
import org.junit.jupiter.api.Test;

class PodStatusFindingEvaluatorTest {

    private final PodStatusFindingEvaluator evaluator = new PodStatusFindingEvaluator();

    @Test
    void should_return_warning_finding_when_pod_phase_is_missing() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodStatusDiagnosticState(null, null));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Pod phase is unavailable");
            assertThat(finding.details()).isEqualTo("Kubernetes did not return a pod phase");
        });
    }

    @Test
    void should_return_warning_finding_when_pod_phase_is_blank() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodStatusDiagnosticState("   ", null));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Pod phase is unavailable");
            assertThat(finding.details()).isEqualTo("Kubernetes did not return a pod phase");
        });
    }

    @Test
    void should_return_info_finding_when_pod_phase_is_running() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodStatusDiagnosticState("Running", null));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Pod phase is Running");
            assertThat(finding.details()).isEqualTo("Kubernetes reports phase=Running");
        });
    }

    @Test
    void should_return_warning_finding_when_pod_phase_is_pending() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodStatusDiagnosticState("Pending", null));

        assertThat(findings).singleElement()
                .extracting(ClusterFinding::severity)
                .isEqualTo(Severity.WARNING);
    }

    @Test
    void should_return_warning_finding_when_pod_phase_is_unknown() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodStatusDiagnosticState("Unknown", null));

        assertThat(findings).singleElement()
                .extracting(ClusterFinding::severity)
                .isEqualTo(Severity.WARNING);
    }

    @Test
    void should_return_critical_finding_when_pod_phase_is_failed() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodStatusDiagnosticState("Failed", null));

        assertThat(findings).singleElement()
                .extracting(ClusterFinding::severity)
                .isEqualTo(Severity.CRITICAL);
    }

    @Test
    void should_return_warning_finding_when_pod_is_terminating() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodStatusDiagnosticState(
                "Running",
                "2026-06-01T13:00:00Z"
        ));

        assertThat(findings)
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.WARNING);
                    assertThat(finding.message()).isEqualTo("Pod is terminating");
                    assertThat(finding.details()).isEqualTo("Deletion timestamp: 2026-06-01T13:00:00Z");
                });
    }

    @Test
    void should_add_no_immediate_issue_finding_when_all_findings_are_info() {
        List<ClusterFinding> findings = evaluator.addNoImmediateIssueFindingWhenAllInfo(List.of(
                new ClusterFinding(Severity.INFO, "Pod phase is Running", "Kubernetes reports phase=Running")
        ));

        assertThat(findings)
                .extracting(ClusterFinding::message)
                .containsExactly("Pod phase is Running", "No immediate pod health issues detected");
    }

    @Test
    void should_not_add_no_immediate_issue_finding_when_any_finding_is_warning() {
        List<ClusterFinding> findings = evaluator.addNoImmediateIssueFindingWhenAllInfo(List.of(
                new ClusterFinding(Severity.INFO, "Pod phase is Running", "Kubernetes reports phase=Running"),
                new ClusterFinding(Severity.WARNING, "Pod is terminating", "Deletion timestamp: now")
        ));

        assertThat(findings)
                .extracting(ClusterFinding::message)
                .doesNotContain("No immediate pod health issues detected");
    }

    @Test
    void should_not_add_no_immediate_issue_finding_when_any_finding_is_critical() {
        List<ClusterFinding> findings = evaluator.addNoImmediateIssueFindingWhenAllInfo(List.of(
                new ClusterFinding(Severity.CRITICAL, "Pod phase is Failed", "Kubernetes reports phase=Failed")
        ));

        assertThat(findings)
                .extracting(ClusterFinding::message)
                .doesNotContain("No immediate pod health issues detected");
    }
}
