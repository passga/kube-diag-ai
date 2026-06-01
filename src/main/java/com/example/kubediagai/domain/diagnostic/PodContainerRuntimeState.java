package com.example.kubediagai.domain.diagnostic;

public record PodContainerRuntimeState(
        int restartCount,
        String waitingReason
) {
    public PodContainerRuntimeState {
        if (waitingReason != null && waitingReason.isBlank()) {
            waitingReason = null;
        }
    }
}
