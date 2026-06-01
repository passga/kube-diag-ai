package com.example.kubediagai.domain.diagnostic;

public record PodConditionDiagnosticState(
        String type,
        String status,
        String reason,
        String message
) {
}
