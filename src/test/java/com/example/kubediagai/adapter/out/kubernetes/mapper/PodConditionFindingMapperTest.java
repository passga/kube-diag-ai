package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.Severity;
import com.example.kubediagai.domain.diagnostic.PodConditionFindingEvaluator;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import org.junit.jupiter.api.Test;

class PodConditionFindingMapperTest {

    private final PodConditionFindingMapper mapper = new PodConditionFindingMapper(
            new PodConditionFindingEvaluator()
    );

    @Test
    void should_translate_fabric8_pod_condition_to_findings_when_condition_has_diagnostic_signal() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
                        .withConditions(new PodConditionBuilder()
                                .withType("PodScheduled")
                                .withStatus("False")
                                .withReason("Unschedulable")
                                .withMessage("0/3 nodes are available")
                                .build())
                        .build())
                .build();

        assertThat(mapper.map(pod))
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.WARNING);
                    assertThat(finding.message()).isEqualTo("Pod condition is not healthy: PodScheduled");
                    assertThat(finding.details())
                            .isEqualTo("status=False, reason=Unschedulable, message=0/3 nodes are available");
                });
    }

    @Test
    void should_return_empty_findings_when_pod_has_no_conditions() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder().build())
                .build();

        assertThat(mapper.map(pod)).isEmpty();
    }
}
