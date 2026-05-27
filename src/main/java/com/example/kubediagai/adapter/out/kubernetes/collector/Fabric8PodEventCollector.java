package com.example.kubediagai.adapter.out.kubernetes.collector;

import com.example.kubediagai.adapter.out.kubernetes.mapper.PodEventFindingMapper;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;

public class Fabric8PodEventCollector {

    private final KubernetesClient client;
    private final PodEventFindingMapper mapper;

    public Fabric8PodEventCollector(KubernetesClient client, PodEventFindingMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public List<ClusterFinding> collect(PodDiagnosticCommand command, Pod pod) {
        try {
            List<Event> events = client.v1()
                    .events()
                    .inNamespace(command.namespace())
                    .withField("involvedObject.kind", "Pod")
                    .withField("involvedObject.name", command.podName())
                    .list()
                    .getItems();

            return mapper.map(events, pod);
        } catch (KubernetesClientException exception) {
            return List.of(mapper.mapUnavailable(exception));
        }
    }
}
