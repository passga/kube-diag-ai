package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.PodHealthStatus;
import com.example.kubediagai.domain.PodSummary;
import com.example.kubediagai.domain.diagnostic.PodHealthEvaluator;
import io.fabric8.kubernetes.api.model.ContainerStateBuilder;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import org.junit.jupiter.api.Test;

class Fabric8PodSummaryMapperTest {

    private final Fabric8PodSummaryMapper mapper = new Fabric8PodSummaryMapper(new PodHealthEvaluator());

    @Test
    void should_map_pod_identity_and_health_when_pod_is_running_ready_and_stable() {
        PodSummary summary = mapper.map(runningReadyPodNamed("pod-ok", stableContainer()));

        assertThat(summary).isEqualTo(new PodSummary(
                "demo",
                "pod-ok",
                "Running",
                true,
                0,
                null,
                PodHealthStatus.HEALTHY
        ));
    }

    @Test
    void should_report_healthy_when_running_ready_pod_has_no_restarts_or_waiting_reason() {
        PodSummary summary = mapper.map(runningReadyPod(
                stableContainer()
        ));

        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.HEALTHY);
    }

    @Test
    void should_report_unhealthy_when_container_is_in_crash_loop_backoff() {
        PodSummary summary = mapper.map(runningNotReadyPod(
                waitingContainer(5, "CrashLoopBackOff")
        ));

        assertThat(summary.restartCount()).isEqualTo(5);
        assertThat(summary.waitingReason()).isEqualTo("CrashLoopBackOff");
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void should_report_unhealthy_when_container_image_pull_is_backing_off() {
        PodSummary summary = mapper.map(pendingNotReadyPod(
                waitingContainer("ImagePullBackOff")
        ));

        assertThat(summary.waitingReason()).isEqualTo("ImagePullBackOff");
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void should_select_unhealthy_waiting_reason_when_later_container_is_crash_looping() {
        PodSummary summary = mapper.map(pendingNotReadyPod(
                waitingContainer("ContainerCreating"),
                waitingContainer(2, "CrashLoopBackOff")
        ));

        assertThat(summary.waitingReason()).isEqualTo("CrashLoopBackOff");
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void should_report_healthy_when_pod_succeeded_even_if_not_ready() {
        PodSummary summary = mapper.map(succeededPod(
                stableContainer()
        ));

        assertThat(summary.ready()).isFalse();
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.HEALTHY);
    }

    @Test
    void should_report_warning_when_running_ready_pod_is_terminating() {
        PodSummary summary = mapper.map(terminatingPod(
                stableContainer()
        ));

        assertThat(summary.ready()).isTrue();
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.WARNING);
    }

    @Test
    void should_report_unhealthy_when_init_container_image_pull_is_backing_off() {
        PodSummary summary = mapper.map(pendingNotReadyPodWithImagePullBackOffInitContainer());

        assertThat(summary.waitingReason()).isEqualTo("ImagePullBackOff");
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void should_report_warning_when_running_ready_pod_has_restarts() {
        PodSummary summary = mapper.map(runningReadyPod(
                restartedContainer()
        ));

        assertThat(summary.restartCount()).isEqualTo(1);
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.WARNING);
    }

    @Test
    void should_report_warning_when_running_pod_is_not_ready() {
        PodSummary summary = mapper.map(runningNotReadyPod(
                stableContainer()
        ));

        assertThat(summary.ready()).isFalse();
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.WARNING);
    }

    private static Pod runningReadyPod(ContainerStatus... containerStatuses) {
        return runningReadyPodNamed("pod", containerStatuses);
    }

    private static Pod runningReadyPodNamed(String name, ContainerStatus... containerStatuses) {
        return pod(name, "Running", true, containerStatuses);
    }

    private static Pod runningNotReadyPod(ContainerStatus... containerStatuses) {
        return pod("pod", "Running", false, containerStatuses);
    }

    private static Pod pendingNotReadyPod(ContainerStatus... containerStatuses) {
        return pod("pod", "Pending", false, containerStatuses);
    }

    private static Pod succeededPod(ContainerStatus... containerStatuses) {
        return pod("pod", "Succeeded", false, containerStatuses);
    }

    private static Pod terminatingPod(ContainerStatus... containerStatuses) {
        return pod("pod", "Running", true, "2026-05-29T15:22:36Z", containerStatuses);
    }

    private static Pod pod(
            String name,
            String phase,
            boolean ready,
            ContainerStatus... containerStatuses
    ) {
        return pod(name, phase, ready, null, containerStatuses);
    }

    private static Pod pod(
            String name,
            String phase,
            boolean ready,
            String deletionTimestamp,
            ContainerStatus... containerStatuses
    ) {
        return podWithInitContainers(
                name,
                phase,
                ready,
                deletionTimestamp,
                containerStatuses,
                new ContainerStatus[]{}
        );
    }

    private static Pod pendingNotReadyPodWithImagePullBackOffInitContainer() {
        return podWithInitContainers(
                "pod",
                "Pending",
                false,
                null,
                new ContainerStatus[]{stableContainer()},
                new ContainerStatus[]{waitingContainer("ImagePullBackOff")}
        );
    }

    private static Pod podWithInitContainers(
            String name,
            String phase,
            boolean ready,
            String deletionTimestamp,
            ContainerStatus[] containerStatuses,
            ContainerStatus[] initContainerStatuses
    ) {
        return new PodBuilder()
                .withNewMetadata()
                .withNamespace("demo")
                .withName(name)
                .withDeletionTimestamp(deletionTimestamp)
                .endMetadata()
                .withStatus(new PodStatusBuilder()
                        .withPhase(phase)
                        .withConditions(new PodConditionBuilder()
                                .withType("Ready")
                                .withStatus(ready ? "True" : "False")
                                .build())
                        .withContainerStatuses(containerStatuses)
                        .withInitContainerStatuses(initContainerStatuses)
                        .build())
                .build();
    }

    private static ContainerStatus stableContainer() {
        return container(0, null);
    }

    private static ContainerStatus restartedContainer() {
        return container(1, null);
    }

    private static ContainerStatus waitingContainer(String waitingReason) {
        return waitingContainer(0, waitingReason);
    }

    private static ContainerStatus waitingContainer(int restartCount, String waitingReason) {
        return container(restartCount, waitingReason);
    }

    private static ContainerStatus container(int restartCount, String waitingReason) {
        ContainerStatusBuilder builder = new ContainerStatusBuilder()
                .withName("container")
                .withRestartCount(restartCount);

        if (waitingReason != null) {
            builder.withState(new ContainerStateBuilder()
                    .withNewWaiting()
                    .withReason(waitingReason)
                    .endWaiting()
                    .build());
        }

        return builder.build();
    }
}
