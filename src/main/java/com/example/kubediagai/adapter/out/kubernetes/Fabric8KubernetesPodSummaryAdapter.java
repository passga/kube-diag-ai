package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.adapter.out.kubernetes.mapper.Fabric8PodSummaryMapper;
import com.example.kubediagai.application.port.out.KubernetesPodSummaryPort;
import com.example.kubediagai.domain.PodSummary;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Comparator;
import java.util.List;

public class Fabric8KubernetesPodSummaryAdapter implements KubernetesPodSummaryPort {

    private final KubernetesClient client;
    private final Fabric8PodSummaryMapper mapper;

    public Fabric8KubernetesPodSummaryAdapter(KubernetesClient client, Fabric8PodSummaryMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<PodSummary> listPods(String namespace) {
        return client.pods()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .map(mapper::map)
                .sorted(Comparator.comparing(PodSummary::name, Comparator.nullsLast(String::compareTo)))
                .toList();
    }
}
