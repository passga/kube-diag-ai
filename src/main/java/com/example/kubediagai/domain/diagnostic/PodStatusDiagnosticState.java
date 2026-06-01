package com.example.kubediagai.domain.diagnostic;

public record PodStatusDiagnosticState(
        String phase,
        String deletionTimestamp
) {
}
