package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import com.example.kubediagai.adapter.out.kubernetes.classifier.KubernetesSeverityClassifier;
import com.example.kubediagai.adapter.out.kubernetes.collector.Fabric8PodEventCollector;
import com.example.kubediagai.adapter.out.kubernetes.collector.Fabric8PodLogCollector;
import com.example.kubediagai.adapter.out.kubernetes.mapper.ContainerStatusFindingMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.Fabric8PodToClusterFindingMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.PodConditionFindingMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.PodEventFindingMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.PodLogFindingMapper;
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
    ContainerStatusFindingMapper containerStatusFindingMapper(
            KubernetesSeverityClassifier kubernetesSeverityClassifier
    ) {
        return new ContainerStatusFindingMapper(kubernetesSeverityClassifier);
    }

    @Bean
    PodConditionFindingMapper podConditionFindingMapper() {
        return new PodConditionFindingMapper();
    }

    @Bean
    Fabric8PodToClusterFindingMapper fabric8PodToClusterFindingMapper(
            KubernetesSeverityClassifier kubernetesSeverityClassifier,
            ContainerStatusFindingMapper containerStatusFindingMapper,
            PodConditionFindingMapper podConditionFindingMapper
    ) {
        return new Fabric8PodToClusterFindingMapper(
                kubernetesSeverityClassifier,
                containerStatusFindingMapper,
                podConditionFindingMapper
        );
    }

    @Bean
    PodLogFindingMapper podLogFindingMapper() {
        return new PodLogFindingMapper();
    }

    @Bean
    PodEventFindingMapper podEventFindingMapper() {
        return new PodEventFindingMapper();
    }

    @Bean
    Fabric8PodLogCollector fabric8PodLogCollector(
            KubernetesClient kubernetesClient,
            PodLogFindingMapper mapper
    ) {
        return new Fabric8PodLogCollector(kubernetesClient, mapper);
    }

    @Bean
    Fabric8PodEventCollector fabric8PodEventCollector(
            KubernetesClient kubernetesClient,
            PodEventFindingMapper mapper
    ) {
        return new Fabric8PodEventCollector(kubernetesClient, mapper);
    }

    @Bean
    KubernetesDiagnosticsPort kubernetesDiagnosticsPort(
            KubernetesClient kubernetesClient,
            Fabric8PodToClusterFindingMapper mapper,
            Fabric8PodLogCollector logCollector,
            Fabric8PodEventCollector eventCollector
    ) {
        return new Fabric8KubernetesDiagnosticsAdapter(kubernetesClient, mapper, logCollector, eventCollector);
    }
}
