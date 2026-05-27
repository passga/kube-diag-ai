package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.EventBuilder;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class PodEventFindingMapperTest {

    private final PodEventFindingMapper mapper = new PodEventFindingMapper();

    @Test
    void mapsWarningEventToWarningFinding() {
        var pod = pod("pod-uid");
        var event = new EventBuilder()
                .withType("Warning")
                .withReason("BackOff")
                .withMessage("Back-off restarting failed container")
                .withCount(3)
                .withLastTimestamp("2026-05-27T09:00:00Z")
                .withInvolvedObject(involvedPod("pod-uid"))
                .build();

        List<ClusterFinding> findings = mapper.map(List.of(event), pod);

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Pod event: BackOff");
            assertThat(finding.details()).contains("type=Warning", "count=3", "Back-off restarting failed container");
        });
    }

    @Test
    void mapsNormalEventToInfoFinding() {
        var event = new EventBuilder()
                .withType("Normal")
                .withReason("Pulled")
                .withMessage("Successfully pulled image")
                .withInvolvedObject(involvedPod("pod-uid"))
                .build();

        List<ClusterFinding> findings = mapper.map(List.of(event), pod("pod-uid"));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Pod event: Pulled");
        });
    }

    @Test
    void ignoresEventsForPreviousPodInstancesWhenUidIsAvailable() {
        var event = new EventBuilder()
                .withReason("BackOff")
                .withInvolvedObject(involvedPod("old-uid"))
                .build();

        List<ClusterFinding> findings = mapper.map(List.of(event), pod("current-uid"));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("No pod events found");
        });
    }

    private static io.fabric8.kubernetes.api.model.Pod pod(String uid) {
        return new PodBuilder()
                .withNewMetadata()
                .withUid(uid)
                .endMetadata()
                .build();
    }

    private static io.fabric8.kubernetes.api.model.ObjectReference involvedPod(String uid) {
        return new ObjectReferenceBuilder()
                .withKind("Pod")
                .withName("pod")
                .withUid(uid)
                .build();
    }
}
