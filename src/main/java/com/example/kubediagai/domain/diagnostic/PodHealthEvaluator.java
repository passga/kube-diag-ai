package com.example.kubediagai.domain.diagnostic;

import com.example.kubediagai.domain.PodHealthStatus;

public class PodHealthEvaluator {

    public PodHealthStatus evaluate(PodRuntimeState state) {
        if ("Failed".equals(state.phase()) || state.hasUnhealthyWaitingReason()) {
            return PodHealthStatus.UNHEALTHY;
        }

        if (state.terminating()) {
            return PodHealthStatus.WARNING;
        }

        if ("Succeeded".equals(state.phase())) {
            return PodHealthStatus.HEALTHY;
        }

        if ("Pending".equals(state.phase())
                || "Unknown".equals(state.phase())
                || !state.ready()
                || state.restartCount() > 0
                || !state.waitingReasons().isEmpty()) {
            return PodHealthStatus.WARNING;
        }

        if ("Running".equals(state.phase())) {
            return PodHealthStatus.HEALTHY;
        }

        return PodHealthStatus.WARNING;
    }
}
