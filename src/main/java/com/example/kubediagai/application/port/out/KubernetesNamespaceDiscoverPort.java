package com.example.kubediagai.application.port.out;

import com.example.kubediagai.domain.NamespaceSummary;

import java.util.List;

public interface KubernetesNamespaceDiscoverPort {
    List<NamespaceSummary> listNamespaces();
}
