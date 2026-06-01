package com.example.kubediagai.domain.diagnostic;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.Objects;

public class PodLogFindingEvaluator {

    private static final int MAX_DETAILS_LENGTH = 4_000;

    public ClusterFinding evaluate(PodLogDiagnosticState state) {
        return switch (state.availability()) {
            case LOGS_AVAILABLE -> logsAvailable(state.containerName(), state.logs());
            case LOGS_UNAVAILABLE -> logsUnavailable(state.containerName(), state.errorMessage());
            case NO_CONTAINERS -> noContainers();
        };
    }

    private static ClusterFinding logsAvailable(String containerName, String logs) {
        if (logs == null || logs.isBlank()) {
            return new ClusterFinding(
                    Severity.INFO,
                    "Recent logs are empty for container " + containerName,
                    "Kubernetes returned no recent log lines for container " + containerName
            );
        }

        return new ClusterFinding(
                Severity.INFO,
                "Recent logs for container " + containerName,
                truncate(logs.strip())
        );
    }

    private static ClusterFinding logsUnavailable(String containerName, String errorMessage) {
        return new ClusterFinding(
                Severity.WARNING,
                "Recent logs unavailable for container " + containerName,
                Objects.requireNonNullElse(errorMessage, "No error details available")
        );
    }

    private static ClusterFinding noContainers() {
        return new ClusterFinding(
                Severity.WARNING,
                "Pod has no containers",
                "No regular or init containers were found in the pod spec"
        );
    }

    private static String truncate(String value) {
        if (value.length() <= MAX_DETAILS_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_DETAILS_LENGTH) + "...";
    }
}
