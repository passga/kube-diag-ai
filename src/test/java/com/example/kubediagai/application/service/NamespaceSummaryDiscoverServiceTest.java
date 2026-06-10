package com.example.kubediagai.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.application.port.out.KubernetesNamespaceDiscoverPort;
import com.example.kubediagai.domain.NamespaceSummary;
import java.util.List;
import org.junit.jupiter.api.Test;

class NamespaceSummaryDiscoverServiceTest {
    @Test
    void should_delegate_namespace_discovery_to_kubernetes_port() {
        NamespaceSummary namespaceSummary = new NamespaceSummary("demo", "OK");
        KubernetesNamespaceDiscoverPort port = () -> List.of(namespaceSummary);

        ListNamespacesService service = new ListNamespacesService(port);

        assertThat(service.listNamespaces()).containsExactly(namespaceSummary);
    }
}
