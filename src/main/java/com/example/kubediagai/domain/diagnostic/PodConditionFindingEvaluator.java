package com.example.kubediagai.domain.diagnostic;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.List;
import java.util.Objects;

public class PodConditionFindingEvaluator {

    public List<ClusterFinding> evaluate(PodConditionDiagnosticState state) {
        if ("True".equals(state.status())) {
            return List.of();
        }

        return List.of(new ClusterFinding(
                Severity.WARNING,
                "Pod condition is not healthy: " + state.type(),
                "status=" + state.status()
                        + ", reason=" + Objects.requireNonNullElse(state.reason(), "none")
                        + ", message=" + Objects.requireNonNullElse(state.message(), "none")
        ));
    }
}
