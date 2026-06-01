package com.example.kubediagai.domain.diagnostic;

public record PodLogDiagnosticState(
        LogAvailability availability,
        String containerName,
        String logs,
        String errorMessage
) {

    public static PodLogDiagnosticState logsAvailable(String containerName, String logs) {
        return new PodLogDiagnosticState(LogAvailability.LOGS_AVAILABLE, containerName, logs, null);
    }

    public static PodLogDiagnosticState logsUnavailable(String containerName, String errorMessage) {
        return new PodLogDiagnosticState(LogAvailability.LOGS_UNAVAILABLE, containerName, null, errorMessage);
    }

    public static PodLogDiagnosticState noContainers() {
        return new PodLogDiagnosticState(LogAvailability.NO_CONTAINERS, null, null, null);
    }

    public enum LogAvailability {
        LOGS_AVAILABLE,
        LOGS_UNAVAILABLE,
        NO_CONTAINERS
    }
}
