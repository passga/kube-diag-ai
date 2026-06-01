package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.diagnostic.PodLogDiagnosticState;
import com.example.kubediagai.domain.diagnostic.PodLogFindingEvaluator;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;

public class PodLogFindingMapper {

    private final PodLogFindingEvaluator evaluator;

    public PodLogFindingMapper(PodLogFindingEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public List<ClusterFinding> map(String containerName, String logs) {
        return List.of(evaluator.evaluate(PodLogDiagnosticState.logsAvailable(containerName, logs)));
    }

    public ClusterFinding mapUnavailable(String containerName, KubernetesClientException exception) {
        String errorMessage = exception == null ? null : exception.getMessage();
        return evaluator.evaluate(PodLogDiagnosticState.logsUnavailable(containerName, errorMessage));
    }

    public ClusterFinding mapNoContainers() {
        return evaluator.evaluate(PodLogDiagnosticState.noContainers());
    }
}
