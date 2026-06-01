package com.example.kubediagai.domain.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContainerStatusFindingEvaluatorTest {

    private final ContainerStatusFindingEvaluator evaluator = new ContainerStatusFindingEvaluator();

    @Test
    void should_return_warning_when_container_has_restarted() {
        List<ClusterFinding> findings = evaluator.evaluate(new ContainerDiagnosticState(
                "app",
                2,
                null,
                null
        ));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Container has restarted");
            assertThat(finding.details()).isEqualTo("app restartCount=2");
        });
    }

    @Test
    void should_return_critical_when_container_waiting_reason_is_crashloopbackoff() {
        List<ClusterFinding> findings = evaluator.evaluate(waitingContainer("CrashLoopBackOff"));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.CRITICAL);
            assertThat(finding.message()).isEqualTo("Container is waiting: CrashLoopBackOff");
            assertThat(finding.details()).isEqualTo("app: waiting message");
        });
    }

    @Test
    void should_return_critical_when_container_waiting_reason_is_imagepullbackoff() {
        List<ClusterFinding> findings = evaluator.evaluate(waitingContainer("ImagePullBackOff"));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.CRITICAL);
            assertThat(finding.message()).isEqualTo("Container is waiting: ImagePullBackOff");
        });
    }

    @Test
    void should_return_critical_when_container_waiting_reason_is_errimagepull() {
        List<ClusterFinding> findings = evaluator.evaluate(waitingContainer("ErrImagePull"));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.CRITICAL);
            assertThat(finding.message()).isEqualTo("Container is waiting: ErrImagePull");
        });
    }

    @Test
    void should_return_info_when_container_waiting_reason_is_containercreating() {
        List<ClusterFinding> findings = evaluator.evaluate(waitingContainer("ContainerCreating"));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Container is waiting: ContainerCreating");
        });
    }

    @Test
    void should_return_warning_when_container_waiting_reason_is_unknown() {
        List<ClusterFinding> findings = evaluator.evaluate(waitingContainer("Waiting"));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Container is waiting: Waiting");
        });
    }

    private static ContainerDiagnosticState waitingContainer(String waitingReason) {
        return new ContainerDiagnosticState("app", 0, waitingReason, "waiting message");
    }
}
