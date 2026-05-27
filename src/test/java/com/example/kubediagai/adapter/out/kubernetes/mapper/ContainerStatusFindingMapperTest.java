package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.adapter.out.kubernetes.classifier.KubernetesSeverityClassifier;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.ContainerStateBuilder;
import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContainerStatusFindingMapperTest {

    private final ContainerStatusFindingMapper mapper = new ContainerStatusFindingMapper(
            new KubernetesSeverityClassifier()
    );

    @Test
    void reportsCrashLoopBackOffAsCritical() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
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
}
