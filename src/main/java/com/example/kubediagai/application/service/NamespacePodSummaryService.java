package com.example.kubediagai.application.service;

import com.example.kubediagai.application.port.in.ListNamespacePodsUseCase;
import com.example.kubediagai.application.port.out.KubernetesPodSummaryPort;
import com.example.kubediagai.domain.PodSummary;
import java.util.List;

public class NamespacePodSummaryService implements ListNamespacePodsUseCase {

    private final KubernetesPodSummaryPort kubernetesPodSummaryPort;

    public NamespacePodSummaryService(KubernetesPodSummaryPort kubernetesPodSummaryPort) {
        this.kubernetesPodSummaryPort = kubernetesPodSummaryPort;
    }

    @Override
    public List<PodSummary> listPods(String namespace) {
        return kubernetesPodSummaryPort.listPods(namespace);
    }
}
