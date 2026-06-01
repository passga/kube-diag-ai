package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import com.example.kubediagai.domain.diagnostic.PodLogDiagnosticState;
import com.example.kubediagai.domain.diagnostic.PodLogFindingEvaluator;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;
import org.junit.jupiter.api.Test;

class PodLogFindingMapperTest {

    private final RecordingPodLogFindingEvaluator evaluator = new RecordingPodLogFindingEvaluator();
    private final PodLogFindingMapper mapper = new PodLogFindingMapper(evaluator);

    @Test
    void should_translate_container_name_and_logs_to_diagnostic_state() {
        List<ClusterFinding> findings = mapper.map("app", "first line");

        assertThat(evaluator.state).isEqualTo(PodLogDiagnosticState.logsAvailable("app", "first line"));
        assertThat(findings).containsExactly(evaluator.finding);
    }

    @Test
    void should_translate_kubernetes_exception_message_to_unavailable_diagnostic_state() {
        ClusterFinding finding = mapper.mapUnavailable("app", new KubernetesClientException("failed"));

        assertThat(evaluator.state).isEqualTo(PodLogDiagnosticState.logsUnavailable("app", "failed"));
        assertThat(finding).isEqualTo(evaluator.finding);
    }

    @Test
    void should_translate_missing_kubernetes_exception_message_to_unavailable_diagnostic_state() {
        ClusterFinding finding = mapper.mapUnavailable("app", new KubernetesClientException((String) null));

        assertThat(evaluator.state).isEqualTo(PodLogDiagnosticState.logsUnavailable("app", null));
        assertThat(finding).isEqualTo(evaluator.finding);
    }

    @Test
    void should_translate_no_containers_to_diagnostic_state() {
        ClusterFinding finding = mapper.mapNoContainers();

        assertThat(evaluator.state).isEqualTo(PodLogDiagnosticState.noContainers());
        assertThat(finding).isEqualTo(evaluator.finding);
    }

    private static class RecordingPodLogFindingEvaluator extends PodLogFindingEvaluator {

        private final ClusterFinding finding = new ClusterFinding(Severity.INFO, "message", "details");
        private PodLogDiagnosticState state;

        @Override
        public ClusterFinding evaluate(PodLogDiagnosticState state) {
            this.state = state;
            return finding;
        }
    }
}
