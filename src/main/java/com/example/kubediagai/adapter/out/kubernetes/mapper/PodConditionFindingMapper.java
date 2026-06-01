package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.diagnostic.PodConditionDiagnosticState;
import com.example.kubediagai.domain.diagnostic.PodConditionFindingEvaluator;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import java.util.List;

public class PodConditionFindingMapper {

    private final PodConditionFindingEvaluator evaluator;

    public PodConditionFindingMapper(PodConditionFindingEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public List<ClusterFinding> map(Pod pod) {
        if (pod.getStatus() == null || pod.getStatus().getConditions() == null) {
            return List.of();
        }

        return pod.getStatus().getConditions().stream()
                .flatMap(condition -> map(condition).stream())
                .toList();
    }

    List<ClusterFinding> map(PodCondition condition) {
        return evaluator.evaluate(new PodConditionDiagnosticState(
                condition.getType(),
                condition.getStatus(),
                condition.getReason(),
                condition.getMessage()
        ));
    }
}
