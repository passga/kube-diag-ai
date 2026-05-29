package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.PodHealthStatus;
import com.example.kubediagai.domain.PodSummary;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Fabric8PodSummaryMapper {

    public PodSummary map(Pod pod) {
        String namespace = pod.getMetadata() == null ? null : pod.getMetadata().getNamespace();
        String name = pod.getMetadata() == null ? null : pod.getMetadata().getName();
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
        String waitingReason = selectWaitingReason(waitingReasons);

        return new PodSummary(
                namespace,
                name,
                phase,
                ready,
                restartCount,
                waitingReason,
                healthStatus(phase, ready, restartCount, waitingReasons)
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

    private static PodHealthStatus healthStatus(
            String phase,
            boolean ready,
            int restartCount,
            List<String> waitingReasons
    ) {
        if ("Failed".equals(phase)
                || waitingReasons.stream().anyMatch(Fabric8PodSummaryMapper::isUnhealthyWaitingReason)) {
            return PodHealthStatus.UNHEALTHY;
        }

        if ("Succeeded".equals(phase)) {
            return PodHealthStatus.HEALTHY;
        }

        if ("Pending".equals(phase)
                || "Unknown".equals(phase)
                || !ready
                || restartCount > 0
                || !waitingReasons.isEmpty()) {
            return PodHealthStatus.WARNING;
        }

        if ("Running".equals(phase)) {
            return PodHealthStatus.HEALTHY;
        }

        return PodHealthStatus.WARNING;
    }

    private static String selectWaitingReason(List<String> waitingReasons) {
        return waitingReasons.stream()
                .filter(Fabric8PodSummaryMapper::isUnhealthyWaitingReason)
                .findFirst()
                .or(() -> waitingReasons.stream().findFirst())
                .orElse(null);
    }

    private static boolean isUnhealthyWaitingReason(String waitingReason) {
        return switch (Objects.requireNonNullElse(waitingReason, "")) {
            case "CrashLoopBackOff", "ImagePullBackOff", "ErrImagePull" -> true;
            default -> false;
        };
    }
}
