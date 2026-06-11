package com.example.kubediagai.adapter.in.rest.dto;

import com.example.kubediagai.domain.NamespaceSummary;

public record NamespaceResponse(
        String name,
        String status
) {
    public static NamespaceResponse from(NamespaceSummary namespace) {
        return new NamespaceResponse(
                namespace.name(),
                namespace.status()
        );
    }
}
