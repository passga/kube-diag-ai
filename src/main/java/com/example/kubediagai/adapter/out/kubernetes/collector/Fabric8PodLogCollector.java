package com.example.kubediagai.adapter.out.kubernetes.collector;

import com.example.kubediagai.adapter.out.kubernetes.mapper.PodLogFindingMapper;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;

public class Fabric8PodLogCollector {

    private static final int TAIL_LINES = 80;

    private final KubernetesClient client;
    private final PodLogFindingMapper mapper;

    public Fabric8PodLogCollector(KubernetesClient client, PodLogFindingMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public List<ClusterFinding> collect(PodDiagnosticCommand command) {
        try {
            String logs = client.pods()
                    .inNamespace(command.namespace())
                    .withName(command.podName())
                    .tailingLines(TAIL_LINES)
                    .getLog();

            return mapper.map(logs);
        } catch (KubernetesClientException exception) {
            return List.of(mapper.mapUnavailable(exception));
        }
    }
}
