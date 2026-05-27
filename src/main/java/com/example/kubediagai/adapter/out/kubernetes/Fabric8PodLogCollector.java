package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;
import java.util.Objects;

class Fabric8PodLogCollector {

    private static final int TAIL_LINES = 80;
    private static final int MAX_DETAILS_LENGTH = 4_000;

    private final KubernetesClient client;

    Fabric8PodLogCollector(KubernetesClient client) {
        this.client = client;
    }

    List<ClusterFinding> collect(PodDiagnosticCommand command) {
        try {
            String logs = client.pods()
                    .inNamespace(command.namespace())
                    .withName(command.podName())
                    .tailingLines(TAIL_LINES)
                    .getLog();

            return mapLogs(logs);
        } catch (KubernetesClientException exception) {
            return List.of(unavailableFinding(exception));
        }
    }

    static List<ClusterFinding> mapLogs(String logs) {
        if (logs == null || logs.isBlank()) {
            return List.of(new ClusterFinding(
                    Severity.INFO,
                    "Recent pod logs are empty",
                    "Kubernetes returned no recent log lines for this pod"
            ));
        }

        return List.of(new ClusterFinding(
                Severity.INFO,
                "Recent pod logs",
                truncate(logs.strip())
        ));
    }

    private static ClusterFinding unavailableFinding(KubernetesClientException exception) {
        return new ClusterFinding(
                Severity.WARNING,
                "Recent pod logs unavailable",
                Objects.requireNonNullElse(exception.getMessage(), "No error details available")
        );
    }

    private static String truncate(String value) {
        if (value.length() <= MAX_DETAILS_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_DETAILS_LENGTH) + "...";
    }
}
