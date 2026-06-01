package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.adapter.out.kubernetes.classifier.KubernetesSeverityClassifier;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import com.example.kubediagai.domain.diagnostic.ContainerStatusFindingEvaluator;
import com.example.kubediagai.domain.diagnostic.PodConditionFindingEvaluator;
import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class Fabric8PodToClusterFindingMapperTest {

    private final Fabric8PodToClusterFindingMapper mapper = new Fabric8PodToClusterFindingMapper(
            new KubernetesSeverityClassifier(),
            new ContainerStatusFindingMapper(new ContainerStatusFindingEvaluator()),
            new PodConditionFindingMapper(new PodConditionFindingEvaluator())
    );

    @Test
    void reportsHealthyPodWithoutWarnings() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Running")
                        .withConditions(new PodConditionBuilder()
                                .withType("Ready")
                                .withStatus("True")
                                .build())
                        .withContainerStatuses(new ContainerStatusBuilder()
                                .withName("ok")
                                .withRestartCount(0)
                                .build())
                        .build())
                .build();

        List<ClusterFinding> findings = mapper.map(pod);

        assertThat(findings)
                .extracting(ClusterFinding::severity)
                .containsOnly(Severity.INFO);
        assertThat(findings)
                .extracting(ClusterFinding::message)
                .contains("No immediate pod health issues detected");
    }

    @Test
    void reportsUnavailablePodPhase() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder().build())
                .build();

        List<ClusterFinding> findings = mapper.map(pod);

        assertThat(findings)
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.WARNING);
                    assertThat(finding.message()).isEqualTo("Pod phase is unavailable");
                });
    }
}
