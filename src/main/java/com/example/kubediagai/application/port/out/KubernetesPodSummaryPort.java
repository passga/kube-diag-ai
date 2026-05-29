package com.example.kubediagai.application.port.out;

import com.example.kubediagai.domain.PodSummary;
import java.util.List;

public interface KubernetesPodSummaryPort {

    List<PodSummary> listPods(String namespace);
}
