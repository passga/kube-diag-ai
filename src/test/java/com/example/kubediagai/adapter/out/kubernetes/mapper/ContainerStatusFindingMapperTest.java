package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import com.example.kubediagai.domain.diagnostic.ContainerStatusFindingEvaluator;
import io.fabric8.kubernetes.api.model.ContainerStateBuilder;
import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContainerStatusFindingMapperTest {

    private final ContainerStatusFindingMapper mapper = new ContainerStatusFindingMapper(
            new ContainerStatusFindingEvaluator()
    );

    @Test
    void should_translate_fabric8_container_status_to_findings_when_container_has_status_signals() {
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
                    assertThat(finding.message()).isEqualTo("Container is waiting: CrashLoopBackOff");
                    assertThat(finding.details()).isEqualTo("crash: back-off restarting failed container");
                })
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.WARNING);
                    assertThat(finding.message()).isEqualTo("Container has restarted");
                    assertThat(finding.details()).isEqualTo("crash restartCount=4");
                });
    }

    @Test
    void should_return_empty_findings_when_pod_has_no_container_statuses() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder().build())
                .build();

        List<ClusterFinding> findings = mapper.map(pod);

        assertThat(findings).isEmpty();
    }
}
