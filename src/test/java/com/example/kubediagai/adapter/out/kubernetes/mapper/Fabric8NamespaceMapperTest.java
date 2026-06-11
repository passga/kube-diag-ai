package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.NamespaceSummary;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import org.junit.jupiter.api.Test;

class Fabric8NamespaceMapperTest {
    @Test
    void should_map_namespace_summary_when_fabric8_namespace_has_metadata_and_status() {
        Namespace namespace = new NamespaceBuilder()
                .withNewMetadata()
                .withName("demo")
                .endMetadata()
                .withNewStatus()
                .withPhase("Active")
                .endStatus()
                .build();

        Fabric8NamespaceMapper mapper = new Fabric8NamespaceMapper();
        NamespaceSummary result = mapper.map(namespace);

        assertThat(result.name()).isEqualTo("demo");
        assertThat(result.status()).isEqualTo("Active");
    }
}
