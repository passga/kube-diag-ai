package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.domain.Severity;

class KubernetesSeverityClassifier {

    Severity classifyPhase(String phase) {
        return switch (phase) {
            case "Failed" -> Severity.CRITICAL;
            case "Pending", "Unknown" -> Severity.WARNING;
            default -> Severity.INFO;
        };
    }

    Severity classifyWaitingReason(String reason) {
        return switch (reason) {
            case "CrashLoopBackOff", "ImagePullBackOff", "ErrImagePull" -> Severity.CRITICAL;
            case "ContainerCreating", "PodInitializing" -> Severity.INFO;
            default -> Severity.WARNING;
        };
    }
}
