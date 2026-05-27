package com.example.kubediagai.adapter.out.kubernetes;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.EventBuilder;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class Fabric8PodEventCollectorTest {

    @Test
    void mapsWarningEventToWarningFinding() {
        var pod = new PodBuilder()
                .withNewMetadata()
                .withUid("pod-uid")
                .endMetadata()
                .build();
        var event = new EventBuilder()
                .withType("Warning")
                .withReason("BackOff")
                .withMessage("Back-off restarting failed container")
                .withCount(3)
                .withLastTimestamp("2026-05-27T09:00:00Z")
                .withInvolvedObject(new ObjectReferenceBuilder()
                        .withKind("Pod")
                        .withName("pod-crashloop")
                        .withUid("pod-uid")
                        .build())
                .build();

        List<ClusterFinding> findings = Fabric8PodEventCollector.mapEvents(List.of(event), pod);

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Pod event: BackOff");
            assertThat(finding.details()).contains("type=Warning", "count=3", "Back-off restarting failed container");
        });
    }

    @Test
    void mapsNormalEventToInfoFinding() {
        var pod = new PodBuilder()
                .withNewMetadata()
                .withUid("pod-uid")
                .endMetadata()
                .build();
        var event = new EventBuilder()
                .withType("Normal")
                .withReason("Pulled")
                .withMessage("Successfully pulled image")
                .withInvolvedObject(new ObjectReferenceBuilder()
                        .withKind("Pod")
                        .withName("pod-ok")
                        .withUid("pod-uid")
                        .build())
                .build();

        List<ClusterFinding> findings = Fabric8PodEventCollector.mapEvents(List.of(event), pod);

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Pod event: Pulled");
        });
    }

    @Test
    void ignoresEventsForPreviousPodInstancesWhenUidIsAvailable() {
        var pod = new PodBuilder()
                .withNewMetadata()
                .withUid("current-uid")
                .endMetadata()
                .build();
        var event = new EventBuilder()
                .withReason("BackOff")
                .withInvolvedObject(new ObjectReferenceBuilder()
                        .withKind("Pod")
                        .withName("pod-crashloop")
                        .withUid("old-uid")
                        .build())
                .build();

        List<ClusterFinding> findings = Fabric8PodEventCollector.mapEvents(List.of(event), pod);

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("No pod events found");
        });
    }
}
