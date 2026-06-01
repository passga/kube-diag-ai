package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.adapter.out.kubernetes.classifier.KubernetesSeverityClassifier;
import com.example.kubediagai.adapter.out.kubernetes.collector.Fabric8PodEventCollector;
import com.example.kubediagai.adapter.out.kubernetes.collector.Fabric8PodLogCollector;
import com.example.kubediagai.adapter.out.kubernetes.mapper.ContainerStatusFindingMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.Fabric8PodSummaryMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.Fabric8PodToClusterFindingMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.PodConditionFindingMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.PodEventFindingMapper;
import com.example.kubediagai.adapter.out.kubernetes.mapper.PodLogFindingMapper;
import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import com.example.kubediagai.domain.diagnostic.ContainerStatusFindingEvaluator;
import com.example.kubediagai.domain.diagnostic.PodConditionFindingEvaluator;
import com.example.kubediagai.domain.diagnostic.PodEventFindingEvaluator;
import com.example.kubediagai.domain.diagnostic.PodHealthEvaluator;
import com.example.kubediagai.domain.diagnostic.PodLogFindingEvaluator;
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
    ContainerStatusFindingEvaluator containerStatusFindingEvaluator() {
        return new ContainerStatusFindingEvaluator();
    }

    @Bean
    ContainerStatusFindingMapper containerStatusFindingMapper(ContainerStatusFindingEvaluator evaluator) {
        return new ContainerStatusFindingMapper(evaluator);
    }

    @Bean
    PodConditionFindingEvaluator podConditionFindingEvaluator() {
        return new PodConditionFindingEvaluator();
    }

    @Bean
    PodConditionFindingMapper podConditionFindingMapper(PodConditionFindingEvaluator evaluator) {
        return new PodConditionFindingMapper(evaluator);
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
    PodLogFindingEvaluator podLogFindingEvaluator() {
        return new PodLogFindingEvaluator();
    }

    @Bean
    PodLogFindingMapper podLogFindingMapper(PodLogFindingEvaluator evaluator) {
        return new PodLogFindingMapper(evaluator);
    }

    @Bean
    PodEventFindingEvaluator podEventFindingEvaluator() {
        return new PodEventFindingEvaluator();
    }

    @Bean
    PodEventFindingMapper podEventFindingMapper(PodEventFindingEvaluator evaluator) {
        return new PodEventFindingMapper(evaluator);
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

    @Bean
    PodHealthEvaluator podHealthEvaluator() {
        return new PodHealthEvaluator();
    }

    @Bean
    Fabric8PodSummaryMapper fabric8PodSummaryMapper(PodHealthEvaluator podHealthEvaluator) {
        return new Fabric8PodSummaryMapper(podHealthEvaluator);
    }

    @Bean
    Fabric8KubernetesPodSummaryAdapter kubernetesPodSummaryPort(
            KubernetesClient kubernetesClient,
            Fabric8PodSummaryMapper mapper
    ) {
        return new Fabric8KubernetesPodSummaryAdapter(kubernetesClient, mapper);
    }
}
