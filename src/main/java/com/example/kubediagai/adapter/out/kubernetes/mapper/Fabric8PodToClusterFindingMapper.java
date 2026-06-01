package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.diagnostic.PodStatusDiagnosticState;
import com.example.kubediagai.domain.diagnostic.PodStatusFindingEvaluator;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.ArrayList;
import java.util.List;

public class Fabric8PodToClusterFindingMapper {

    private final PodStatusFindingEvaluator podStatusFindingEvaluator;
    private final ContainerStatusFindingMapper containerStatusFindingMapper;
    private final PodConditionFindingMapper podConditionFindingMapper;

    public Fabric8PodToClusterFindingMapper(
            PodStatusFindingEvaluator podStatusFindingEvaluator,
            ContainerStatusFindingMapper containerStatusFindingMapper,
            PodConditionFindingMapper podConditionFindingMapper
    ) {
        this.podStatusFindingEvaluator = podStatusFindingEvaluator;
        this.containerStatusFindingMapper = containerStatusFindingMapper;
        this.podConditionFindingMapper = podConditionFindingMapper;
    }

    public List<ClusterFinding> map(Pod pod) {
        List<ClusterFinding> findings = new ArrayList<>();

        String phase = pod.getStatus() == null ? null : pod.getStatus().getPhase();
        String deletionTimestamp = pod.getMetadata() == null ? null : pod.getMetadata().getDeletionTimestamp();

        findings.addAll(podStatusFindingEvaluator.evaluate(new PodStatusDiagnosticState(phase, deletionTimestamp)));
        findings.addAll(containerStatusFindingMapper.map(pod));
        findings.addAll(podConditionFindingMapper.map(pod));

        return podStatusFindingEvaluator.addNoImmediateIssueFindingWhenAllInfo(findings);
    }
}
