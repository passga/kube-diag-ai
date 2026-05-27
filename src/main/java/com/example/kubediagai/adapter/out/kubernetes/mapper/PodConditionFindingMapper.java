package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import java.util.List;
import java.util.Objects;

public class PodConditionFindingMapper {

    public List<ClusterFinding> map(Pod pod) {
        if (pod.getStatus() == null || pod.getStatus().getConditions() == null) {
            return List.of();
        }

        return pod.getStatus().getConditions().stream()
                .filter(condition -> !"True".equals(condition.getStatus()))
                .map(this::map)
                .toList();
    }

    ClusterFinding map(PodCondition condition) {
        return new ClusterFinding(
                Severity.WARNING,
                "Pod condition is not healthy: " + condition.getType(),
                "status=" + condition.getStatus()
                        + ", reason=" + Objects.requireNonNullElse(condition.getReason(), "none")
                        + ", message=" + Objects.requireNonNullElse(condition.getMessage(), "none")
        );
    }
}
