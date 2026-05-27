package com.example.kubediagai.adapter.out.kubernetes.classifier;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.Severity;
import org.junit.jupiter.api.Test;

class KubernetesSeverityClassifierTest {

    private final KubernetesSeverityClassifier classifier = new KubernetesSeverityClassifier();

    @Test
    void classifiesPodPhases() {
        assertThat(classifier.classifyPhase("Failed")).isEqualTo(Severity.CRITICAL);
        assertThat(classifier.classifyPhase("Pending")).isEqualTo(Severity.WARNING);
        assertThat(classifier.classifyPhase("Unknown")).isEqualTo(Severity.WARNING);
        assertThat(classifier.classifyPhase("Running")).isEqualTo(Severity.INFO);
        assertThat(classifier.classifyPhase("Succeeded")).isEqualTo(Severity.INFO);
    }

    @Test
    void classifiesContainerWaitingReasons() {
        assertThat(classifier.classifyWaitingReason("CrashLoopBackOff")).isEqualTo(Severity.CRITICAL);
        assertThat(classifier.classifyWaitingReason("ImagePullBackOff")).isEqualTo(Severity.CRITICAL);
        assertThat(classifier.classifyWaitingReason("ErrImagePull")).isEqualTo(Severity.CRITICAL);
        assertThat(classifier.classifyWaitingReason("ContainerCreating")).isEqualTo(Severity.INFO);
        assertThat(classifier.classifyWaitingReason("PodInitializing")).isEqualTo(Severity.INFO);
        assertThat(classifier.classifyWaitingReason("CreateContainerConfigError")).isEqualTo(Severity.WARNING);
    }
}
