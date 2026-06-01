package com.example.kubediagai.domain.diagnostic;

import java.util.List;
import java.util.Objects;

public record PodRuntimeState(
        String phase,
        boolean ready,
        List<PodContainerRuntimeState> containers,
        boolean terminating
) {
    public PodRuntimeState {
        containers = List.copyOf(Objects.requireNonNullElse(containers, List.of()));
    }

    public int restartCount() {
        return containers.stream()
                .mapToInt(PodContainerRuntimeState::restartCount)
                .sum();
    }

    public List<String> waitingReasons() {
        return containers.stream()
                .map(PodContainerRuntimeState::waitingReason)
                .filter(Objects::nonNull)
                .toList();
    }

    public String selectedWaitingReason() {
        List<String> reasons = waitingReasons();

        return reasons.stream()
                .filter(PodRuntimeState::isUnhealthyWaitingReason)
                .findFirst()
                .or(() -> reasons.stream().findFirst())
                .orElse(null);
    }

    public boolean hasUnhealthyWaitingReason() {
        return waitingReasons().stream()
                .anyMatch(PodRuntimeState::isUnhealthyWaitingReason);
    }

    private static boolean isUnhealthyWaitingReason(String waitingReason) {
        return switch (Objects.requireNonNullElse(waitingReason, "")) {
            case "CrashLoopBackOff", "ImagePullBackOff", "ErrImagePull" -> true;
            default -> false;
        };
    }
}
