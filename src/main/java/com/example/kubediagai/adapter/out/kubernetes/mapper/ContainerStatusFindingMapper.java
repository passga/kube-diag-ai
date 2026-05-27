package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.adapter.out.kubernetes.classifier.KubernetesSeverityClassifier;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContainerStatusFindingMapper {

    private final KubernetesSeverityClassifier severityClassifier;

    public ContainerStatusFindingMapper(KubernetesSeverityClassifier severityClassifier) {
        this.severityClassifier = severityClassifier;
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
        List<ClusterFinding> findings = new ArrayList<>();

        if (status.getRestartCount() != null && status.getRestartCount() > 0) {
            findings.add(new ClusterFinding(
                    Severity.WARNING,
                    "Container has restarted",
                    status.getName() + " restartCount=" + status.getRestartCount()
            ));
        }

        if (status.getState() == null || status.getState().getWaiting() == null) {
            return findings;
        }

        ContainerStateWaiting waiting = status.getState().getWaiting();
        String reason = Objects.requireNonNullElse(waiting.getReason(), "Waiting");
        findings.add(new ClusterFinding(
                severityClassifier.classifyWaitingReason(reason),
                "Container is waiting: " + reason,
                status.getName() + ": " + Objects.requireNonNullElse(waiting.getMessage(), "No message")
        ));

        return List.copyOf(findings);
    }
}
