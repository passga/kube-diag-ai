package com.example.kubediagai.application.port.in;

import com.example.kubediagai.domain.PodSummary;
import java.util.List;

public interface ListNamespacePodsUseCase {

    List<PodSummary> listPods(String namespace);
}
