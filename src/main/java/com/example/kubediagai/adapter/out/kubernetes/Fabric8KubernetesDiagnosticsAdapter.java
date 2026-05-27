package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import com.example.kubediagai.adapter.out.kubernetes.collector.Fabric8PodEventCollector;
import com.example.kubediagai.adapter.out.kubernetes.collector.Fabric8PodLogCollector;
import com.example.kubediagai.adapter.out.kubernetes.mapper.Fabric8PodToClusterFindingMapper;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Fabric8KubernetesDiagnosticsAdapter implements KubernetesDiagnosticsPort {

    private final KubernetesClient client;
    private final Fabric8PodToClusterFindingMapper mapper;
    private final Fabric8PodLogCollector logCollector;
    private final Fabric8PodEventCollector eventCollector;

    public Fabric8KubernetesDiagnosticsAdapter(
            KubernetesClient client,
            Fabric8PodToClusterFindingMapper mapper,
            Fabric8PodLogCollector logCollector,
            Fabric8PodEventCollector eventCollector
    ) {
        this.client = client;
        this.mapper = mapper;
        this.logCollector = logCollector;
        this.eventCollector = eventCollector;
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

            List<ClusterFinding> findings = new ArrayList<>(mapper.map(pod));
            findings.addAll(logCollector.collect(command, pod));
            findings.addAll(eventCollector.collect(command, pod));

            return List.copyOf(findings);
        } catch (KubernetesClientException exception) {
            return List.of(new ClusterFinding(
                    Severity.CRITICAL,
                    "Kubernetes API request failed",
                    Objects.requireNonNullElse(exception.getMessage(), "No error details available")
            ));
        }
    }
}
