package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesAdapterConfiguration {

    @Bean
    KubernetesClient kubernetesClient() {
        return new KubernetesClientBuilder().build();
    }

    @Bean
    KubernetesSeverityClassifier kubernetesSeverityClassifier() {
        return new KubernetesSeverityClassifier();
    }

    @Bean
    Fabric8PodToClusterFindingMapper fabric8PodToClusterFindingMapper(
            KubernetesSeverityClassifier kubernetesSeverityClassifier
    ) {
        return new Fabric8PodToClusterFindingMapper(kubernetesSeverityClassifier);
    }

    @Bean
    KubernetesDiagnosticsPort kubernetesDiagnosticsPort(
            KubernetesClient kubernetesClient,
            Fabric8PodToClusterFindingMapper mapper
    ) {
        return new Fabric8KubernetesDiagnosticsAdapter(kubernetesClient, mapper);
    }
}
