package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.PodHealthStatus;
import com.example.kubediagai.domain.PodSummary;
import io.fabric8.kubernetes.api.model.ContainerStateBuilder;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import org.junit.jupiter.api.Test;

class Fabric8PodSummaryMapperTest {

    private final Fabric8PodSummaryMapper mapper = new Fabric8PodSummaryMapper();

    @Test
    void mapsHealthyPod() {
        PodSummary summary = mapper.map(pod("demo", "pod-ok", "Running", true, container("app", true, 0, null)));

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
    void mapsCrashLoopBackOffPodAsUnhealthy() {
        PodSummary summary = mapper.map(pod(
                "demo",
                "pod-crashloop",
                "Running",
                false,
                container("app", false, 5, "CrashLoopBackOff")
        ));

        assertThat(summary.restartCount()).isEqualTo(5);
        assertThat(summary.waitingReason()).isEqualTo("CrashLoopBackOff");
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void mapsImagePullBackOffPodAsUnhealthy() {
        PodSummary summary = mapper.map(pod(
                "demo",
                "pod-imagepullbackoff",
                "Pending",
                false,
                container("app", false, 0, "ImagePullBackOff")
        ));

        assertThat(summary.waitingReason()).isEqualTo("ImagePullBackOff");
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.UNHEALTHY);
    }

    @Test
    void mapsRestartedPodAsWarning() {
        PodSummary summary = mapper.map(pod(
                "demo",
                "pod-restarted",
                "Running",
                true,
                container("app", true, 1, null)
        ));

        assertThat(summary.restartCount()).isEqualTo(1);
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.WARNING);
    }

    @Test
    void mapsNotReadyPodAsWarning() {
        PodSummary summary = mapper.map(pod(
                "demo",
                "pod-not-ready",
                "Running",
                false,
                container("app", false, 0, null)
        ));

        assertThat(summary.ready()).isFalse();
        assertThat(summary.healthStatus()).isEqualTo(PodHealthStatus.WARNING);
    }

    private static Pod pod(
            String namespace,
            String name,
            String phase,
            boolean ready,
            ContainerStatus... containerStatuses
    ) {
        return new PodBuilder()
                .withNewMetadata()
                .withNamespace(namespace)
                .withName(name)
                .endMetadata()
                .withStatus(new PodStatusBuilder()
                        .withPhase(phase)
                        .withConditions(new PodConditionBuilder()
                                .withType("Ready")
                                .withStatus(ready ? "True" : "False")
                                .build())
                        .withContainerStatuses(containerStatuses)
                        .build())
                .build();
    }

    private static ContainerStatus container(String name, boolean ready, int restartCount, String waitingReason) {
        ContainerStatusBuilder builder = new ContainerStatusBuilder()
                .withName(name)
                .withReady(ready)
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
