package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.diagnostic.ContainerDiagnosticState;
import com.example.kubediagai.domain.diagnostic.ContainerStatusFindingEvaluator;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContainerStatusFindingMapper {

    private final ContainerStatusFindingEvaluator evaluator;

    public ContainerStatusFindingMapper(ContainerStatusFindingEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public List<ClusterFinding> map(Pod pod) {
        if (pod.getStatus() == null || pod.getStatus().getContainerStatuses() == null) {
            return List.of();
        }

        List<ClusterFinding> findings = new ArrayList<>();
        for (ContainerStatus status : pod.getStatus().getContainerStatuses()) {
            findings.addAll(map(status));
        }

        return List.copyOf(findings);
    }

    List<ClusterFinding> map(ContainerStatus status) {
        ContainerStateWaiting waiting = status.getState() == null ? null : status.getState().getWaiting();
        ContainerDiagnosticState state = new ContainerDiagnosticState(
                status.getName(),
                Objects.requireNonNullElse(status.getRestartCount(), 0),
                waiting == null ? null : Objects.requireNonNullElse(waiting.getReason(), "Waiting"),
                waiting == null ? null : waiting.getMessage()
        );

        return evaluator.evaluate(state);
    }
}
