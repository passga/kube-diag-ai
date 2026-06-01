package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import com.example.kubediagai.domain.diagnostic.ContainerStatusFindingEvaluator;
import com.example.kubediagai.domain.diagnostic.PodConditionFindingEvaluator;
import com.example.kubediagai.domain.diagnostic.PodStatusDiagnosticState;
import com.example.kubediagai.domain.diagnostic.PodStatusFindingEvaluator;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class Fabric8PodToClusterFindingMapperTest {

    private final RecordingPodStatusFindingEvaluator podStatusFindingEvaluator =
            new RecordingPodStatusFindingEvaluator();
    private final RecordingContainerStatusFindingMapper containerStatusFindingMapper =
            new RecordingContainerStatusFindingMapper();
    private final RecordingPodConditionFindingMapper podConditionFindingMapper =
            new RecordingPodConditionFindingMapper();
    private final Fabric8PodToClusterFindingMapper mapper = new Fabric8PodToClusterFindingMapper(
            podStatusFindingEvaluator,
            containerStatusFindingMapper,
            podConditionFindingMapper
    );

    @Test
    void should_extract_fabric8_pod_phase_to_diagnostic_state() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Running")
                        .build())
                .build();

        mapper.map(pod);

        assertThat(podStatusFindingEvaluator.state).isEqualTo(new PodStatusDiagnosticState("Running", null));
    }

    @Test
    void should_extract_fabric8_deletion_timestamp_to_diagnostic_state() {
        var pod = new PodBuilder()
                .withNewMetadata()
                .withDeletionTimestamp("2026-06-01T13:00:00Z")
                .endMetadata()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Running")
                        .build())
                .build();

        mapper.map(pod);

        assertThat(podStatusFindingEvaluator.state)
                .isEqualTo(new PodStatusDiagnosticState("Running", "2026-06-01T13:00:00Z"));
    }

    @Test
    void should_delegate_to_container_and_condition_mappers() {
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Running")
                        .build())
                .build();

        List<ClusterFinding> findings = mapper.map(pod);

        assertThat(containerStatusFindingMapper.pod).isSameAs(pod);
        assertThat(podConditionFindingMapper.pod).isSameAs(pod);
        assertThat(findings)
                .extracting(ClusterFinding::message)
                .contains("container finding", "condition finding");
    }

    @Test
    void should_preserve_final_healthy_summary_when_all_findings_are_info() {
        var mapper = new Fabric8PodToClusterFindingMapper(
                new PodStatusFindingEvaluator(),
                new RecordingContainerStatusFindingMapper(),
                new RecordingPodConditionFindingMapper()
        );
        var pod = new PodBuilder()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Running")
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

    private static class RecordingPodStatusFindingEvaluator extends PodStatusFindingEvaluator {

        private PodStatusDiagnosticState state;

        @Override
        public List<ClusterFinding> evaluate(PodStatusDiagnosticState state) {
            this.state = state;
            return List.of(new ClusterFinding(
                    Severity.INFO,
                    "Pod phase is " + state.phase(),
                    "Kubernetes reports phase=" + state.phase()
            ));
        }

        @Override
        public List<ClusterFinding> addNoImmediateIssueFindingWhenAllInfo(List<ClusterFinding> findings) {
            List<ClusterFinding> evaluatedFindings = new ArrayList<>(findings);
            evaluatedFindings.add(new ClusterFinding(
                    Severity.INFO,
                    "No immediate pod health issues detected",
                    "Pod status did not expose waiting containers, failed phases, or failing conditions"
            ));
            return List.copyOf(evaluatedFindings);
        }
    }

    private static class RecordingContainerStatusFindingMapper extends ContainerStatusFindingMapper {

        private Pod pod;

        private RecordingContainerStatusFindingMapper() {
            super(new ContainerStatusFindingEvaluator());
        }

        @Override
        public List<ClusterFinding> map(Pod pod) {
            this.pod = pod;
            return List.of(new ClusterFinding(Severity.INFO, "container finding", "container details"));
        }
    }

    private static class RecordingPodConditionFindingMapper extends PodConditionFindingMapper {

        private Pod pod;

        private RecordingPodConditionFindingMapper() {
            super(new PodConditionFindingEvaluator());
        }

        @Override
        public List<ClusterFinding> map(Pod pod) {
            this.pod = pod;
            return List.of(new ClusterFinding(Severity.INFO, "condition finding", "condition details"));
        }
    }
}
