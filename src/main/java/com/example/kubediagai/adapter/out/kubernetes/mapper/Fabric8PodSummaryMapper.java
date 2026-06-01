package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.PodHealthEvaluator;
import com.example.kubediagai.domain.PodSummary;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.List;
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
        int restartCount = containerStatuses(pod)
                .map(ContainerStatus::getRestartCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        List<String> waitingReasons = containerStatuses(pod)
                .map(ContainerStatus::getState)
                .filter(Objects::nonNull)
                .map(state -> state.getWaiting())
                .filter(Objects::nonNull)
                .map(ContainerStateWaiting::getReason)
                .filter(reason -> reason != null && !reason.isBlank())
                .toList();
        String waitingReason = podHealthEvaluator.selectWaitingReason(waitingReasons);

        return new PodSummary(
                namespace,
                name,
                phase,
                ready,
                restartCount,
                waitingReason,
                podHealthEvaluator.evaluate(phase, ready, restartCount, waitingReasons, terminating)
        );
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
