package com.example.kubediagai.domain.diagnostic;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContainerStatusFindingEvaluator {

    public List<ClusterFinding> evaluate(ContainerDiagnosticState state) {
        List<ClusterFinding> findings = new ArrayList<>();

        if (state.restartCount() > 0) {
            findings.add(new ClusterFinding(
                    Severity.WARNING,
                    "Container has restarted",
                    state.name() + " restartCount=" + state.restartCount()
            ));
        }

        if (state.waitingReason() == null) {
            return List.copyOf(findings);
        }

        String reason = Objects.requireNonNullElse(state.waitingReason(), "Waiting");
        findings.add(new ClusterFinding(
                classifyWaitingReason(reason),
                "Container is waiting: " + reason,
                state.name() + ": " + Objects.requireNonNullElse(state.waitingMessage(), "No message")
        ));

        return List.copyOf(findings);
    }

    private static Severity classifyWaitingReason(String reason) {
        return switch (reason) {
            case "CrashLoopBackOff", "ImagePullBackOff", "ErrImagePull" -> Severity.CRITICAL;
            case "ContainerCreating", "PodInitializing" -> Severity.INFO;
            default -> Severity.WARNING;
        };
    }
}
