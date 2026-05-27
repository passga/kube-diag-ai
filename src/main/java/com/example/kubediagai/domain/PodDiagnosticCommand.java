package com.example.kubediagai.domain;

import java.util.Objects;

public record PodDiagnosticCommand(
        String namespace,
        String podName
) {
    public PodDiagnosticCommand {
        requireText(namespace, "namespace");
        requireText(podName, "podName");
    }

    private static void requireText(String value, String fieldName) {
        if (Objects.requireNonNullElse(value, "").isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
