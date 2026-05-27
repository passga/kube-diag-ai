package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import org.junit.jupiter.api.Test;

class PodConditionFindingMapperTest {

    private final PodConditionFindingMapper mapper = new PodConditionFindingMapper();

    @Test
    void reportsUnhealthyPodConditions() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
                        .withConditions(new PodConditionBuilder()
                                .withType("Ready")
                                .withStatus("False")
                                .withReason("ContainersNotReady")
                                .withMessage("containers with unready status")
                                .build())
                        .build())
                .build();

        assertThat(mapper.map(pod))
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.WARNING);
                    assertThat(finding.message()).contains("Ready");
                    assertThat(finding.details()).contains("ContainersNotReady");
                });
    }
}
