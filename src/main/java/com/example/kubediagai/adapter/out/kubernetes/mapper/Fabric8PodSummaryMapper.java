package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.PodSummary;
import com.example.kubediagai.domain.diagnostic.PodContainerRuntimeState;
import com.example.kubediagai.domain.diagnostic.PodHealthEvaluator;
import com.example.kubediagai.domain.diagnostic.PodRuntimeState;
import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.Objects;
import java.util.stream.Stream;

public class Fabric8PodSummaryMapper {

    private final PodHealthEvaluator podHealthEvaluator;

    public Fabric8PodSummaryMapper(PodHealthEvaluator podHealthEvaluator) {
        this.podHealthEvaluator = podHealthEvaluator;
    }

    public PodSummary map(Pod pod) {
        String namespace = pod.getMetadata() == null ? null : pod.getMetadata().getNamespace();
        String name = pod.getMetadata() == null ? null : pod.getMetadata().getName();
        boolean terminating = pod.getMetadata() != null && pod.getMetadata().getDeletionTimestamp() != null;
        String phase = pod.getStatus() == null ? null : pod.getStatus().getPhase();
        boolean ready = isReady(pod);
        PodRuntimeState runtimeState = new PodRuntimeState(
                phase,
                ready,
                containerStatuses(pod)
                        .map(Fabric8PodSummaryMapper::containerRuntimeState)
                        .toList(),
                terminating
        );

        return new PodSummary(
                namespace,
                name,
                runtimeState.phase(),
                runtimeState.ready(),
                runtimeState.restartCount(),
                runtimeState.selectedWaitingReason(),
                podHealthEvaluator.evaluate(runtimeState)
        );
    }

    private static PodContainerRuntimeState containerRuntimeState(ContainerStatus containerStatus) {
        return new PodContainerRuntimeState(
                Objects.requireNonNullElse(containerStatus.getRestartCount(), 0),
                waitingReason(containerStatus)
        );
    }

    private static String waitingReason(ContainerStatus containerStatus) {
        return Stream.ofNullable(containerStatus.getState())
                .map(ContainerState::getWaiting)
                .filter(Objects::nonNull)
                .map(waiting -> waiting.getReason())
                .findFirst()
                .orElse(null);
    }

    private static boolean isReady(Pod pod) {
        if (pod.getStatus() == null || pod.getStatus().getConditions() == null) {
            return false;
        }

        return pod.getStatus().getConditions().stream()
                .anyMatch(condition -> "Ready".equals(condition.getType()) && "True".equals(condition.getStatus()));
    }

    private static Stream<ContainerStatus> containerStatuses(Pod pod) {
        if (pod.getStatus() == null) {
            return Stream.empty();
        }

        return Stream.concat(
                pod.getStatus().getContainerStatuses() == null
                        ? Stream.empty()
                        : pod.getStatus().getContainerStatuses().stream(),
                pod.getStatus().getInitContainerStatuses() == null
                        ? Stream.empty()
                        : pod.getStatus().getInitContainerStatuses().stream()
        );
    }

}
