package com.example.kubediagai.domain.diagnostic;

public record ContainerDiagnosticState(
        String name,
        int restartCount,
        String waitingReason,
        String waitingMessage
) {
}
