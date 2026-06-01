package com.example.kubediagai.domain.diagnostic;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.List;
import java.util.Objects;

public class PodEventFindingEvaluator {

    public List<ClusterFinding> evaluate(List<PodEventDiagnosticState> states) {
        if (states == null || states.isEmpty()) {
            return List.of(noEventsFinding("Kubernetes returned no events for this pod"));
        }

        return states.stream()
                .map(this::evaluate)
                .toList();
    }

    private ClusterFinding evaluate(PodEventDiagnosticState state) {
        return switch (state.availability()) {
            case NO_EVENTS_AVAILABLE -> noEventsFinding("Kubernetes returned no events for this pod");
            case NO_MATCHING_EVENTS -> noEventsFinding("Kubernetes returned no events matching this pod instance");
            case EVENT -> eventFinding(state);
        };
    }

    private static ClusterFinding eventFinding(PodEventDiagnosticState state) {
        String type = Objects.requireNonNullElse(state.type(), "Normal");
        String reason = Objects.requireNonNullElse(state.reason(), "Unknown");
        String message = Objects.requireNonNullElse(state.message(), "No message");
        Integer count = Objects.requireNonNullElse(state.count(), 1);
        String timestamp = Objects.requireNonNullElse(state.timestamp(), "");

        return new ClusterFinding(
                "Warning".equals(type) ? Severity.WARNING : Severity.INFO,
                "Pod event: " + reason,
                "type=" + type
                        + ", count=" + count
                        + ", time=" + timestamp
                        + ", message=" + message
        );
    }

    private static ClusterFinding noEventsFinding(String details) {
        return new ClusterFinding(Severity.INFO, "No pod events found", details);
    }
}
