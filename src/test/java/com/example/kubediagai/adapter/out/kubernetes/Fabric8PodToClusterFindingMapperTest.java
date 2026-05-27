package com.example.kubediagai.adapter.out.kubernetes;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.ContainerStateBuilder;
import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class Fabric8PodToClusterFindingMapperTest {

    private final Fabric8PodToClusterFindingMapper mapper = new Fabric8PodToClusterFindingMapper(
            new KubernetesSeverityClassifier()
    );

    @Test
    void reportsCrashLoopBackOffAsCritical() {
        var pod = new PodBuilder()
                .withNewMetadata()
                .withName("pod-crashloop")
                .withNamespace("demo")
                .endMetadata()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Running")
                        .withContainerStatuses(new ContainerStatusBuilder()
                                .withName("crash")
                                .withRestartCount(4)
                                .withState(new ContainerStateBuilder()
                                        .withNewWaiting()
                                        .withReason("CrashLoopBackOff")
                                        .withMessage("back-off restarting failed container")
                                        .endWaiting()
                                        .build())
                                .build())
                        .build())
                .build();

        List<ClusterFinding> findings = mapper.map(pod);

        assertThat(findings)
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.CRITICAL);
                    assertThat(finding.message()).contains("CrashLoopBackOff");
                })
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.WARNING);
                    assertThat(finding.details()).contains("restartCount=4");
                });
    }

    @Test
    void reportsImagePullBackOffAsCritical() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Pending")
                        .withContainerStatuses(new ContainerStatusBuilder()
                                .withName("bad-image")
                                .withRestartCount(0)
                                .withState(new ContainerStateBuilder()
                                        .withNewWaiting()
                                        .withReason("ImagePullBackOff")
                                        .withMessage("Back-off pulling image")
                                        .endWaiting()
                                        .build())
                                .build())
                        .build())
                .build();

        List<ClusterFinding> findings = mapper.map(pod);

        assertThat(findings)
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.CRITICAL);
                    assertThat(finding.message()).contains("ImagePullBackOff");
                });
    }

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
    void reportsUnhealthyPodConditions() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Running")
                        .withConditions(new PodConditionBuilder()
                                .withType("Ready")
                                .withStatus("False")
                                .withReason("ContainersNotReady")
                                .withMessage("containers with unready status")
                                .build())
                        .build())
                .build();

        List<ClusterFinding> findings = mapper.map(pod);

        assertThat(findings)
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.WARNING);
                    assertThat(finding.message()).contains("Ready");
                    assertThat(finding.details()).contains("ContainersNotReady");
                });
    }
}
