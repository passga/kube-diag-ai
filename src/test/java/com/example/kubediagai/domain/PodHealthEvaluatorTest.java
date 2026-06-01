package com.example.kubediagai.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PodHealthEvaluatorTest {

    private final PodHealthEvaluator evaluator = new PodHealthEvaluator();

    @Test
    void classifiesFailedPodAsUnhealthy() {
        PodHealthStatus healthStatus = evaluator.evaluate("Failed", true, 0, List.of(), false);

        assertThat(healthStatus).isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void classifiesUnhealthyWaitingReasonsAsUnhealthy() {
        assertThat(evaluator.evaluate("Running", false, 5, List.of("CrashLoopBackOff"), false))
                .isEqualTo(PodHealthStatus.UNHEALTHY);
        assertThat(evaluator.evaluate("Pending", false, 0, List.of("ImagePullBackOff"), false))
                .isEqualTo(PodHealthStatus.UNHEALTHY);
        assertThat(evaluator.evaluate("Pending", false, 0, List.of("ErrImagePull"), false))
                .isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void classifiesSucceededPodAsHealthyEvenWhenNotReady() {
        PodHealthStatus healthStatus = evaluator.evaluate("Succeeded", false, 0, List.of(), false);

        assertThat(healthStatus).isEqualTo(PodHealthStatus.HEALTHY);
    }

    @Test
    void classifiesRunningReadyPodWithoutRestartsOrWaitingReasonAsHealthy() {
        PodHealthStatus healthStatus = evaluator.evaluate("Running", true, 0, List.of(), false);

        assertThat(healthStatus).isEqualTo(PodHealthStatus.HEALTHY);
    }

    @Test
    void classifiesTerminatingPodAsWarning() {
        PodHealthStatus healthStatus = evaluator.evaluate("Running", true, 0, List.of(), true);

        assertThat(healthStatus).isEqualTo(PodHealthStatus.WARNING);
    }

    @Test
    void classifiesRecoverableSignalsAsWarning() {
        assertThat(evaluator.evaluate("Pending", true, 0, List.of(), false))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate("Unknown", true, 0, List.of(), false))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate("Running", false, 0, List.of(), false))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate("Running", true, 1, List.of(), false))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate("Running", true, 0, List.of("ContainerCreating"), false))
                .isEqualTo(PodHealthStatus.WARNING);
        assertThat(evaluator.evaluate(null, true, 0, List.of(), false))
                .isEqualTo(PodHealthStatus.WARNING);
    }

    @Test
    void selectsUnhealthyWaitingReasonBeforeOtherWaitingReasons() {
        String reason = evaluator.selectWaitingReason(List.of("ContainerCreating", "CrashLoopBackOff"));

        assertThat(reason).isEqualTo("CrashLoopBackOff");
    }

    @Test
    void selectsFirstWaitingReasonWhenNoUnhealthyReasonExists() {
        String reason = evaluator.selectWaitingReason(List.of("ContainerCreating", "PodInitializing"));

        assertThat(reason).isEqualTo("ContainerCreating");
    }

    @Test
    void selectsNoWaitingReasonWhenNoneExists() {
        assertThat(evaluator.selectWaitingReason(List.of())).isNull();
        assertThat(evaluator.selectWaitingReason(null)).isNull();
    }
}
