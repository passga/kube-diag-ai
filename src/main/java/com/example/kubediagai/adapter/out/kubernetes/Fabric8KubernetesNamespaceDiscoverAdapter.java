package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.adapter.out.kubernetes.mapper.Fabric8NamespaceMapper;
import com.example.kubediagai.application.port.out.KubernetesNamespaceDiscoverPort;
import com.example.kubediagai.domain.NamespaceSummary;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;


public class Fabric8KubernetesNamespaceDiscoverAdapter implements KubernetesNamespaceDiscoverPort {
    private final KubernetesClient client;
    private final Fabric8NamespaceMapper mapper;

    public Fabric8KubernetesNamespaceDiscoverAdapter(KubernetesClient client, Fabric8NamespaceMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<NamespaceSummary> listNamespaces() {
      return client.namespaces().list().getItems().stream().map(mapper::map).toList();
    }
}
