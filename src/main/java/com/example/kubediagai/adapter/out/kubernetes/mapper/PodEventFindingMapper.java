package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PodEventFindingMapper {

    private static final int MAX_EVENTS = 10;

    public List<ClusterFinding> map(List<Event> events, Pod pod) {
        if (events == null || events.isEmpty()) {
            return List.of(noEventsFinding("Kubernetes returned no events for this pod"));
        }

        String podUid = pod.getMetadata() == null ? null : pod.getMetadata().getUid();

        List<ClusterFinding> findings = events.stream()
                .filter(event -> belongsToPod(event, podUid))
                .sorted(Comparator.comparing(PodEventFindingMapper::eventTimestamp).reversed())
                .limit(MAX_EVENTS)
                .map(this::map)
                .toList();

        if (findings.isEmpty()) {
            return List.of(noEventsFinding("Kubernetes returned no events matching this pod instance"));
        }

        return findings;
    }

    public ClusterFinding mapUnavailable(KubernetesClientException exception) {
        return new ClusterFinding(
                Severity.WARNING,
                "Pod events unavailable",
                Objects.requireNonNullElse(exception.getMessage(), "No error details available")
        );
    }

    private ClusterFinding map(Event event) {
        String type = Objects.requireNonNullElse(event.getType(), "Normal");
        String reason = Objects.requireNonNullElse(event.getReason(), "Unknown");
        String message = Objects.requireNonNullElse(event.getMessage(), "No message");
        Integer count = Objects.requireNonNullElse(event.getCount(), 1);

        return new ClusterFinding(
                "Warning".equals(type) ? Severity.WARNING : Severity.INFO,
                "Pod event: " + reason,
                "type=" + type
                        + ", count=" + count
                        + ", time=" + eventTimestamp(event)
                        + ", message=" + message
        );
    }

    private static boolean belongsToPod(Event event, String podUid) {
        if (podUid == null || podUid.isBlank()) {
            return true;
        }

        ObjectReference involvedObject = event.getInvolvedObject();
        return involvedObject == null || Objects.equals(podUid, involvedObject.getUid());
    }

    private static ClusterFinding noEventsFinding(String details) {
        return new ClusterFinding(Severity.INFO, "No pod events found", details);
    }

    private static String eventTimestamp(Event event) {
        if (event.getLastTimestamp() != null) {
            return event.getLastTimestamp();
        }
        if (event.getFirstTimestamp() != null) {
            return event.getFirstTimestamp();
        }
        return "";
    }
}
