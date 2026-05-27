package com.example.kubediagai.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PodDiagnosticCommandTest {

    @Test
    void requiresNamespace() {
        assertThatThrownBy(() -> new PodDiagnosticCommand("", "api-0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("namespace is required");
    }

    @Test
    void requiresPodName() {
        assertThatThrownBy(() -> new PodDiagnosticCommand("default", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("podName is required");
    }
}
