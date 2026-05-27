package com.example.kubediagai.adapter.out.kubernetes.collector;

import com.example.kubediagai.adapter.out.kubernetes.mapper.PodLogFindingMapper;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Fabric8PodLogCollector {

    private static final int TAIL_LINES = 80;

    private final KubernetesClient client;
    private final PodLogFindingMapper mapper;

    public Fabric8PodLogCollector(KubernetesClient client, PodLogFindingMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public List<ClusterFinding> collect(PodDiagnosticCommand command, Pod pod) {
        List<String> containerNames = containerNames(pod);
        if (containerNames.isEmpty()) {
            return List.of(mapper.mapNoContainers());
        }

        List<ClusterFinding> findings = new ArrayList<>();
        for (String containerName : containerNames) {
            try {
                String logs = client.pods()
                        .inNamespace(command.namespace())
                        .withName(command.podName())
                        .inContainer(containerName)
                        .tailingLines(TAIL_LINES)
                        .getLog();

                findings.addAll(mapper.map(containerName, logs));
            } catch (KubernetesClientException exception) {
                findings.add(mapper.mapUnavailable(containerName, exception));
            }
        }

        return List.copyOf(findings);
    }

    private static List<String> containerNames(Pod pod) {
        if (pod == null || pod.getSpec() == null) {
            return List.of();
        }

        return Stream.concat(
                        nullSafeStream(pod.getSpec().getContainers()),
                        nullSafeStream(pod.getSpec().getInitContainers())
                )
                .map(Container::getName)
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .toList();
    }

    private static <T> Stream<T> nullSafeStream(List<T> values) {
        return values == null ? Stream.empty() : values.stream();
    }
}
