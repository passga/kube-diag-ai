package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;
import java.util.Objects;

public class PodLogFindingMapper {

    private static final int MAX_DETAILS_LENGTH = 4_000;

    public List<ClusterFinding> map(String logs) {
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

    public ClusterFinding mapUnavailable(KubernetesClientException exception) {
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
