package com.example.kubediagai.domain;

public record ClusterFinding(
        Severity severity,
        String message,
        String details
) {
}
