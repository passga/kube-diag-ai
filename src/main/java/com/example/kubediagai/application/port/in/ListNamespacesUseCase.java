package com.example.kubediagai.application.port.in;

import com.example.kubediagai.domain.NamespaceSummary;

import java.util.List;

public interface ListNamespacesUseCase {
    List<NamespaceSummary> listNamespaces();
}
