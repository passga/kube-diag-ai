package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;
import java.util.Objects;

public class PodLogFindingMapper {

    private static final int MAX_DETAILS_LENGTH = 4_000;

    public List<ClusterFinding> map(String containerName, String logs) {
        if (logs == null || logs.isBlank()) {
            return List.of(new ClusterFinding(
                    Severity.INFO,
                    "Recent logs are empty for container " + containerName,
                    "Kubernetes returned no recent log lines for container " + containerName
            ));
        }

        return List.of(new ClusterFinding(
                Severity.INFO,
                "Recent logs for container " + containerName,
                truncate(logs.strip())
        ));
    }

    public ClusterFinding mapUnavailable(String containerName, KubernetesClientException exception) {
        return new ClusterFinding(
                Severity.WARNING,
                "Recent logs unavailable for container " + containerName,
                Objects.requireNonNullElse(exception.getMessage(), "No error details available")
        );
    }

    public ClusterFinding mapNoContainers() {
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
