package com.example.kubediagai.adapter.out.kubernetes.classifier;

import com.example.kubediagai.domain.Severity;

public class KubernetesSeverityClassifier {

    public Severity classifyPhase(String phase) {
        return switch (phase) {
            case "Failed" -> Severity.CRITICAL;
            case "Pending", "Unknown" -> Severity.WARNING;
            default -> Severity.INFO;
        };
    }

    public Severity classifyWaitingReason(String reason) {
        return switch (reason) {
            case "CrashLoopBackOff", "ImagePullBackOff", "ErrImagePull" -> Severity.CRITICAL;
            case "ContainerCreating", "PodInitializing" -> Severity.INFO;
            default -> Severity.WARNING;
        };
    }
}
