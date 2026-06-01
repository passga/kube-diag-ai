package com.example.kubediagai.domain.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.List;
import org.junit.jupiter.api.Test;

class PodEventFindingEvaluatorTest {

    private final PodEventFindingEvaluator evaluator = new PodEventFindingEvaluator();

    @Test
    void should_return_info_finding_when_no_events_are_available() {
        List<ClusterFinding> findings = evaluator.evaluate(List.of(PodEventDiagnosticState.noEventsAvailable()));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("No pod events found");
            assertThat(finding.details()).isEqualTo("Kubernetes returned no events for this pod");
        });
    }

    @Test
    void should_return_info_finding_when_no_events_match_pod() {
        List<ClusterFinding> findings = evaluator.evaluate(List.of(PodEventDiagnosticState.noMatchingEvents()));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("No pod events found");
            assertThat(finding.details()).isEqualTo("Kubernetes returned no events matching this pod instance");
        });
    }

    @Test
    void should_return_warning_finding_when_event_type_is_warning() {
        List<ClusterFinding> findings = evaluator.evaluate(List.of(PodEventDiagnosticState.event(
                "Warning",
                "BackOff",
                "Back-off restarting failed container",
                3,
                "2026-05-27T09:00:00Z"
        )));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Pod event: BackOff");
            assertThat(finding.details()).isEqualTo(
                    "type=Warning, count=3, time=2026-05-27T09:00:00Z, "
                            + "message=Back-off restarting failed container"
            );
        });
    }

    @Test
    void should_return_info_finding_when_event_type_is_normal() {
        List<ClusterFinding> findings = evaluator.evaluate(List.of(PodEventDiagnosticState.event(
                "Normal",
                "Pulled",
                "Successfully pulled image",
                1,
                "2026-05-27T08:00:00Z"
        )));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Pod event: Pulled");
        });
    }

    @Test
    void should_use_fallback_values_when_event_fields_are_missing() {
        List<ClusterFinding> findings = evaluator.evaluate(List.of(PodEventDiagnosticState.event(
                null,
                null,
                null,
                null,
                null
        )));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Pod event: Unknown");
            assertThat(finding.details()).isEqualTo("type=Normal, count=1, time=, message=No message");
        });
    }
}
