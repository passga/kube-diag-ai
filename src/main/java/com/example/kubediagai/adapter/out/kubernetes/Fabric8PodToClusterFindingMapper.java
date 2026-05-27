package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Fabric8PodToClusterFindingMapper {

    private final KubernetesSeverityClassifier severityClassifier;

    Fabric8PodToClusterFindingMapper(KubernetesSeverityClassifier severityClassifier) {
        this.severityClassifier = severityClassifier;
    }

    List<ClusterFinding> map(Pod pod) {
        List<ClusterFinding> findings = new ArrayList<>();

        String phase = pod.getStatus() == null ? null : pod.getStatus().getPhase();
        if (phase == null || phase.isBlank()) {
            findings.add(new ClusterFinding(
                    Severity.WARNING,
                    "Pod phase is unavailable",
                    "Kubernetes did not return a pod phase"
            ));
        } else {
            findings.add(new ClusterFinding(
                    severityClassifier.classifyPhase(phase),
                    "Pod phase is " + phase,
                    "Kubernetes reports phase=" + phase
            ));
        }

        if (pod.getMetadata() != null && pod.getMetadata().getDeletionTimestamp() != null) {
            findings.add(new ClusterFinding(
                    Severity.WARNING,
                    "Pod is terminating",
                    "Deletion timestamp: " + pod.getMetadata().getDeletionTimestamp()
            ));
        }

        collectContainerFindings(pod, findings);
        collectConditionFindings(pod, findings);

        if (findings.stream().allMatch(finding -> finding.severity() == Severity.INFO)) {
            findings.add(new ClusterFinding(
                    Severity.INFO,
                    "No immediate pod health issues detected",
                    "Pod status did not expose waiting containers, failed phases, or failing conditions"
            ));
        }

        return List.copyOf(findings);
    }

    private void collectContainerFindings(Pod pod, List<ClusterFinding> findings) {
        if (pod.getStatus() == null || pod.getStatus().getContainerStatuses() == null) {
            return;
        }

        for (ContainerStatus status : pod.getStatus().getContainerStatuses()) {
            if (status.getRestartCount() != null && status.getRestartCount() > 0) {
                findings.add(new ClusterFinding(
                        Severity.WARNING,
                        "Container has restarted",
                        status.getName() + " restartCount=" + status.getRestartCount()
                ));
            }

            if (status.getState() == null || status.getState().getWaiting() == null) {
                continue;
            }

            ContainerStateWaiting waiting = status.getState().getWaiting();
            String reason = Objects.requireNonNullElse(waiting.getReason(), "Waiting");
            findings.add(new ClusterFinding(
                    severityClassifier.classifyWaitingReason(reason),
                    "Container is waiting: " + reason,
                    status.getName() + ": " + Objects.requireNonNullElse(waiting.getMessage(), "No message")
            ));
        }
    }

    private static void collectConditionFindings(Pod pod, List<ClusterFinding> findings) {
        if (pod.getStatus() == null || pod.getStatus().getConditions() == null) {
            return;
        }

        for (PodCondition condition : pod.getStatus().getConditions()) {
            if ("True".equals(condition.getStatus()) || "PodScheduled".equals(condition.getType())) {
                continue;
            }

            findings.add(new ClusterFinding(
                    Severity.WARNING,
                    "Pod condition is not healthy: " + condition.getType(),
                    "status=" + condition.getStatus()
                            + ", reason=" + Objects.requireNonNullElse(condition.getReason(), "none")
                            + ", message=" + Objects.requireNonNullElse(condition.getMessage(), "none")
            ));
        }
    }
}
