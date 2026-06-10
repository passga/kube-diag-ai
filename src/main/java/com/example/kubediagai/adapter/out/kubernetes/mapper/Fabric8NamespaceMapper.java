package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.NamespaceSummary;
import io.fabric8.kubernetes.api.model.Namespace;

public class Fabric8NamespaceMapper {
    public NamespaceSummary map(Namespace namespace) {
        return new NamespaceSummary(namespace.getMetadata().getName(),
                namespace.getStatus().getPhase());
    }
}
