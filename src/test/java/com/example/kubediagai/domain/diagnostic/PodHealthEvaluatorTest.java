package com.example.kubediagai.domain.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.PodHealthStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class PodHealthEvaluatorTest {

    private final PodHealthEvaluator evaluator = new PodHealthEvaluator();

    @Test
    void should_return_unhealthy_when_pod_phase_is_failed() {
        PodHealthStatus healthStatus = evaluator.evaluate(pod("Failed", true, List.of(), false));

        assertThat(healthStatus).isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void should_return_unhealthy_when_waiting_reason_is_unhealthy() {
        assertThat(evaluator.evaluate(pod("Running", false, List.of(waitingContainer(5, "CrashLoopBackOff")), false)))
                .isEqualTo(PodHealthStatus.UNHEALTHY);
        assertThat(evaluator.evaluate(pod("Pending", false, List.of(waitingContainer("ImagePullBackOff")), false)))
                .isEqualTo(PodHealthStatus.UNHEALTHY);
        assertThat(evaluator.evaluate(pod("Pending", false, List.of(waitingContainer("ErrImagePull")), false)))
                .isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void should_return_healthy_when_pod_succeeded_even_if_not_ready() {
        PodHealthStatus healthStatus = evaluator.evaluate(pod("Succeeded", false, List.of(stableContainer()), false));

        assertThat(healthStatus).isEqualTo(PodHealthStatus.HEALTHY);
    }

    @Test
    void should_return_healthy_when_pod_is_running_ready_and_stable() {
        PodHealthStatus healthStatus = evaluator.evaluate(pod("Running", true, List.of(stableContainer()), false));

        assertThat(healthStatus).isEqualTo(PodHealthStatus.HEALTHY);
    }

    @Test
    void should_return_warning_when_pod_is_terminating() {
        PodHealthStatus healthStatus = evaluator.evaluate(pod("Running", true, List.of(stableContainer()), true));

        assertThat(healthStatus).isEqualTo(PodHealthStatus.WARNING);
    }

    @Test
    void should_return_warning_when_pod_has_recoverable_diagnostic_signals() {
        assertThat(evaluator.evaluate(pod("Pending", true, List.of(stableContainer()), false)))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate(pod("Unknown", true, List.of(stableContainer()), false)))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate(pod("Running", false, List.of(stableContainer()), false)))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate(pod("Running", true, List.of(restartedContainer()), false)))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate(pod("Running", true, List.of(waitingContainer("ContainerCreating")), false)))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate(pod(null, true, List.of(stableContainer()), false)))
                .isEqualTo(PodHealthStatus.WARNING);
    }

    private static PodRuntimeState pod(
            String phase,
            boolean ready,
            List<PodContainerRuntimeState> containers,
            boolean terminating
    ) {
        return new PodRuntimeState(phase, ready, containers, terminating);
    }

    private static PodContainerRuntimeState stableContainer() {
        return new PodContainerRuntimeState(0, null);
    }

    private static PodContainerRuntimeState restartedContainer() {
        return new PodContainerRuntimeState(1, null);
    }

    private static PodContainerRuntimeState waitingContainer(String waitingReason) {
        return waitingContainer(0, waitingReason);
    }

    private static PodContainerRuntimeState waitingContainer(int restartCount, String waitingReason) {
        return new PodContainerRuntimeState(restartCount, waitingReason);
    }
}
