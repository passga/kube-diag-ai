package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.adapter.out.kubernetes.classifier.KubernetesSeverityClassifier;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.ArrayList;
import java.util.List;

public class Fabric8PodToClusterFindingMapper {

    private final KubernetesSeverityClassifier severityClassifier;
    private final ContainerStatusFindingMapper containerStatusFindingMapper;
    private final PodConditionFindingMapper podConditionFindingMapper;

    public Fabric8PodToClusterFindingMapper(
            KubernetesSeverityClassifier severityClassifier,
            ContainerStatusFindingMapper containerStatusFindingMapper,
            PodConditionFindingMapper podConditionFindingMapper
    ) {
        this.severityClassifier = severityClassifier;
        this.containerStatusFindingMapper = containerStatusFindingMapper;
        this.podConditionFindingMapper = podConditionFindingMapper;
    }

    public List<ClusterFinding> map(Pod pod) {
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

        findings.addAll(containerStatusFindingMapper.map(pod));
        findings.addAll(podConditionFindingMapper.map(pod));

        if (findings.stream().allMatch(finding -> finding.severity() == Severity.INFO)) {
            findings.add(new ClusterFinding(
                    Severity.INFO,
                    "No immediate pod health issues detected",
                    "Pod status did not expose waiting containers, failed phases, or failing conditions"
            ));
        }

        return List.copyOf(findings);
    }
}
