package com.example.kubediagai.domain;

public record PodSummary(
        String namespace,
        String name,
        String phase,
        boolean ready,
        int restartCount,
        String waitingReason,
        PodHealthStatus healthStatus
) {
}
