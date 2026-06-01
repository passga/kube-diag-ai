package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import com.example.kubediagai.domain.diagnostic.PodEventDiagnosticState;
import com.example.kubediagai.domain.diagnostic.PodEventFindingEvaluator;
import io.fabric8.kubernetes.api.model.EventBuilder;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;
import org.junit.jupiter.api.Test;

class PodEventFindingMapperTest {

    private final PodEventFindingMapper mapper = new PodEventFindingMapper(
            new PodEventFindingEvaluator()
    );

    @Test
    void should_translate_fabric8_event_fields_to_diagnostic_state() {
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
            assertThat(finding.message()).isEqualTo("Pod event: BackOff");
            assertThat(finding.details()).isEqualTo(
                    "type=Warning, count=3, time=2026-05-27T09:00:00Z, "
                            + "message=Back-off restarting failed container"
            );
        });
    }

    @Test
    void should_sort_matching_events_by_latest_timestamp_before_limiting() {
        var pod = pod("pod-uid");
        var oldEvent = event("Started", "2026-05-27T08:00:00Z", "pod-uid");
        var latestEvent = event("BackOff", "2026-05-27T09:00:00Z", "pod-uid");

        List<ClusterFinding> findings = mapper.map(List.of(oldEvent, latestEvent), pod);

        assertThat(findings)
                .extracting(ClusterFinding::message)
                .containsExactly("Pod event: BackOff", "Pod event: Started");
    }

    @Test
    void should_ignore_events_for_previous_pod_instances_when_uid_is_available() {
        var event = new EventBuilder()
                .withReason("BackOff")
                .withInvolvedObject(involvedPod("old-uid"))
                .build();

        List<ClusterFinding> findings = mapper.map(List.of(event), pod("current-uid"));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.message()).isEqualTo("No pod events found");
        });
    }

    @Test
    void should_keep_only_ten_latest_matching_events() {
        var pod = pod("pod-uid");
        List<io.fabric8.kubernetes.api.model.Event> events = java.util.stream.IntStream.rangeClosed(1, 11)
                .mapToObj(index -> event(
                        "Event" + index,
                        "2026-05-27T09:%02d:00Z".formatted(index),
                        "pod-uid"
                ))
                .toList();

        List<ClusterFinding> findings = mapper.map(events, pod);

        assertThat(findings).hasSize(10);
        assertThat(findings)
                .extracting(ClusterFinding::message)
                .doesNotContain("Pod event: Event1");
    }

    @Test
    void should_translate_kubernetes_exception_message_to_unavailable_event_diagnostic_state() {
        var evaluator = new RecordingPodEventFindingEvaluator();
        var mapper = new PodEventFindingMapper(evaluator);

        ClusterFinding finding = mapper.mapUnavailable(new KubernetesClientException("failed"));

        assertThat(evaluator.states).containsExactly(PodEventDiagnosticState.eventsUnavailable("failed"));
        assertThat(finding).isEqualTo(evaluator.finding);
    }

    @Test
    void should_translate_missing_kubernetes_exception_message_to_unavailable_event_diagnostic_state() {
        var evaluator = new RecordingPodEventFindingEvaluator();
        var mapper = new PodEventFindingMapper(evaluator);

        ClusterFinding finding = mapper.mapUnavailable(new KubernetesClientException((String) null));

        assertThat(evaluator.states).containsExactly(PodEventDiagnosticState.eventsUnavailable(null));
        assertThat(finding).isEqualTo(evaluator.finding);
    }

    @Test
    void should_translate_null_kubernetes_exception_to_unavailable_event_diagnostic_state() {
        var evaluator = new RecordingPodEventFindingEvaluator();
        var mapper = new PodEventFindingMapper(evaluator);

        ClusterFinding finding = mapper.mapUnavailable(null);

        assertThat(evaluator.states).containsExactly(PodEventDiagnosticState.eventsUnavailable(null));
        assertThat(finding).isEqualTo(evaluator.finding);
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

    private static io.fabric8.kubernetes.api.model.Event event(String reason, String lastTimestamp, String uid) {
        return new EventBuilder()
                .withType("Normal")
                .withReason(reason)
                .withMessage("event " + reason)
                .withLastTimestamp(lastTimestamp)
                .withInvolvedObject(involvedPod(uid))
                .build();
    }

    private static class RecordingPodEventFindingEvaluator extends PodEventFindingEvaluator {

        private final ClusterFinding finding = new ClusterFinding(Severity.INFO, "message", "details");
        private List<PodEventDiagnosticState> states;

        @Override
        public List<ClusterFinding> evaluate(List<PodEventDiagnosticState> states) {
            this.states = states;
            return List.of(finding);
        }
    }
}
