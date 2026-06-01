package com.example.kubediagai.domain.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.List;
import org.junit.jupiter.api.Test;

class PodConditionFindingEvaluatorTest {

    private final PodConditionFindingEvaluator evaluator = new PodConditionFindingEvaluator();

    @Test
    void should_return_empty_findings_when_condition_status_is_true() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodConditionDiagnosticState(
                "Ready",
                "True",
                null,
                null
        ));

        assertThat(findings).isEmpty();
    }

    @Test
    void should_return_warning_when_condition_status_is_false() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodConditionDiagnosticState(
                "Ready",
                "False",
                "ContainersNotReady",
                "containers with unready status"
        ));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Pod condition is not healthy: Ready");
            assertThat(finding.details())
                    .isEqualTo("status=False, reason=ContainersNotReady, message=containers with unready status");
        });
    }

    @Test
    void should_use_none_when_reason_is_missing() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodConditionDiagnosticState(
                "Ready",
                "False",
                null,
                "containers with unready status"
        ));

        assertThat(findings).singleElement()
                .extracting(ClusterFinding::details)
                .isEqualTo("status=False, reason=none, message=containers with unready status");
    }

    @Test
    void should_use_none_when_message_is_missing() {
        List<ClusterFinding> findings = evaluator.evaluate(new PodConditionDiagnosticState(
                "Ready",
                "False",
                "ContainersNotReady",
                null
        ));

        assertThat(findings).singleElement()
                .extracting(ClusterFinding::details)
                .isEqualTo("status=False, reason=ContainersNotReady, message=none");
    }
}
