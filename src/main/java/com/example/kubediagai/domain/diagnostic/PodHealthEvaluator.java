package com.example.kubediagai.domain.diagnostic;

import com.example.kubediagai.domain.PodHealthStatus;
import java.util.List;
import java.util.Objects;

public class PodHealthEvaluator {

    public PodHealthStatus evaluate(
            String phase,
            boolean ready,
            int restartCount,
            List<String> waitingReasons,
            boolean terminating
    ) {
        List<String> reasons = Objects.requireNonNullElse(waitingReasons, List.of());

        if ("Failed".equals(phase)
                || reasons.stream().anyMatch(this::isUnhealthyWaitingReason)) {
            return PodHealthStatus.UNHEALTHY;
        }

        if (terminating) {
            return PodHealthStatus.WARNING;
        }

        if ("Succeeded".equals(phase)) {
            return PodHealthStatus.HEALTHY;
        }

        if ("Pending".equals(phase)
                || "Unknown".equals(phase)
                || !ready
                || restartCount > 0
                || !reasons.isEmpty()) {
            return PodHealthStatus.WARNING;
        }

        if ("Running".equals(phase)) {
            return PodHealthStatus.HEALTHY;
        }

        return PodHealthStatus.WARNING;
    }

    public String selectWaitingReason(List<String> waitingReasons) {
        List<String> reasons = Objects.requireNonNullElse(waitingReasons, List.of());

        return reasons.stream()
                .filter(this::isUnhealthyWaitingReason)
                .findFirst()
                .or(() -> reasons.stream().findFirst())
                .orElse(null);
    }

    private boolean isUnhealthyWaitingReason(String waitingReason) {
        return switch (Objects.requireNonNullElse(waitingReason, "")) {
            case "CrashLoopBackOff", "ImagePullBackOff", "ErrImagePull" -> true;
            default -> false;
        };
    }
}
