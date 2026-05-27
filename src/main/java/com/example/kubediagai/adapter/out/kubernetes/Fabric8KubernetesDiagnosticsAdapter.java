package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;
import java.util.Objects;

public class Fabric8KubernetesDiagnosticsAdapter implements KubernetesDiagnosticsPort {

    private final KubernetesClient client;
    private final Fabric8PodToClusterFindingMapper mapper;

    public Fabric8KubernetesDiagnosticsAdapter(
            KubernetesClient client,
            Fabric8PodToClusterFindingMapper mapper
    ) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<ClusterFinding> collectPodFindings(PodDiagnosticCommand command) {
        try {
            Pod pod = client.pods()
                    .inNamespace(command.namespace())
                    .withName(command.podName())
                    .get();

            if (pod == null) {
                return List.of(new ClusterFinding(
                        Severity.CRITICAL,
                        "Pod not found",
                        "No pod named " + command.namespace() + "/" + command.podName()
                ));
            }

            return mapper.map(pod);
        } catch (KubernetesClientException exception) {
            return List.of(new ClusterFinding(
                    Severity.CRITICAL,
                    "Kubernetes API request failed",
                    Objects.requireNonNullElse(exception.getMessage(), "No error details available")
            ));
        }
    }
}
