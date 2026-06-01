package com.example.kubediagai.domain.diagnostic;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.ArrayList;
import java.util.List;

public class PodStatusFindingEvaluator {

    public List<ClusterFinding> evaluate(PodStatusDiagnosticState state) {
        List<ClusterFinding> findings = new ArrayList<>();

        if (state.phase() == null || state.phase().isBlank()) {
            findings.add(new ClusterFinding(
                    Severity.WARNING,
                    "Pod phase is unavailable",
                    "Kubernetes did not return a pod phase"
            ));
        } else {
            findings.add(new ClusterFinding(
                    classifyPhase(state.phase()),
                    "Pod phase is " + state.phase(),
                    "Kubernetes reports phase=" + state.phase()
            ));
        }

        if (state.deletionTimestamp() != null) {
            findings.add(new ClusterFinding(
                    Severity.WARNING,
                    "Pod is terminating",
                    "Deletion timestamp: " + state.deletionTimestamp()
            ));
        }

        return List.copyOf(findings);
    }

    public List<ClusterFinding> addNoImmediateIssueFindingWhenAllInfo(List<ClusterFinding> findings) {
        List<ClusterFinding> evaluatedFindings = new ArrayList<>(findings);

        if (evaluatedFindings.stream().allMatch(finding -> finding.severity() == Severity.INFO)) {
            evaluatedFindings.add(new ClusterFinding(
                    Severity.INFO,
                    "No immediate pod health issues detected",
                    "Pod status did not expose waiting containers, failed phases, or failing conditions"
            ));
        }

        return List.copyOf(evaluatedFindings);
    }

    private static Severity classifyPhase(String phase) {
        return switch (phase) {
            case "Failed" -> Severity.CRITICAL;
            case "Pending", "Unknown" -> Severity.WARNING;
            default -> Severity.INFO;
        };
    }
}
