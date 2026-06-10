package com.example.kubediagai.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.application.port.out.KubernetesPodSummaryPort;
import com.example.kubediagai.domain.PodHealthStatus;
import com.example.kubediagai.domain.PodSummary;
import java.util.List;
import org.junit.jupiter.api.Test;

class NamespacePodSummaryServiceTest {

    @Test
    void delegatesNamespacePodListingToKubernetesPort() {
        PodSummary pod = new PodSummary("demo", "pod-ok", "Running", true, 0, null, PodHealthStatus.HEALTHY);
        KubernetesPodSummaryPort port = namespace -> {
            assertThat(namespace).isEqualTo("demo");
            return List.of(pod);
        };
        NamespacePodSummaryService service = new NamespacePodSummaryService(port);

        assertThat(service.listPods("demo")).containsExactly(pod);
    }
}
