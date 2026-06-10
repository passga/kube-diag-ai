package com.example.kubediagai.application.service;

import com.example.kubediagai.application.port.in.ListNamespacesUseCase;
import com.example.kubediagai.application.port.out.KubernetesNamespaceDiscoverPort;
import com.example.kubediagai.domain.NamespaceSummary;

import java.util.List;

public class ListNamespacesService implements ListNamespacesUseCase {
    private final KubernetesNamespaceDiscoverPort kubernetesNamespaceDiscoverPort;

    public ListNamespacesService(KubernetesNamespaceDiscoverPort kubernetesNamespaceDiscoverPort){
        this.kubernetesNamespaceDiscoverPort = kubernetesNamespaceDiscoverPort;
    }

    @Override
    public List<NamespaceSummary> listNamespaces() {
        return kubernetesNamespaceDiscoverPort.listNamespaces();
    }
}
