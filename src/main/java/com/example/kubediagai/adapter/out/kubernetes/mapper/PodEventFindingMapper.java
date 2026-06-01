package com.example.kubediagai.adapter.out.kubernetes.mapper;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.diagnostic.PodEventDiagnosticState;
import com.example.kubediagai.domain.diagnostic.PodEventFindingEvaluator;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PodEventFindingMapper {

    private static final int MAX_EVENTS = 10;

    private final PodEventFindingEvaluator evaluator;

    public PodEventFindingMapper(PodEventFindingEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public List<ClusterFinding> map(List<Event> events, Pod pod) {
        if (events == null || events.isEmpty()) {
            return evaluator.evaluate(List.of(PodEventDiagnosticState.noEventsAvailable()));
        }

        String podUid = pod.getMetadata() == null ? null : pod.getMetadata().getUid();

        List<PodEventDiagnosticState> states = events.stream()
                .filter(event -> belongsToPod(event, podUid))
                .sorted(Comparator.comparing(PodEventFindingMapper::eventTimestamp).reversed())
                .limit(MAX_EVENTS)
                .map(PodEventFindingMapper::map)
                .toList();

        if (states.isEmpty()) {
            return evaluator.evaluate(List.of(PodEventDiagnosticState.noMatchingEvents()));
        }

        return evaluator.evaluate(states);
    }

    public ClusterFinding mapUnavailable(KubernetesClientException exception) {
        String errorMessage = exception == null ? null : exception.getMessage();
        return evaluator.evaluate(List.of(PodEventDiagnosticState.eventsUnavailable(errorMessage))).get(0);
    }

    private static PodEventDiagnosticState map(Event event) {
        return PodEventDiagnosticState.event(
                event.getType(),
                event.getReason(),
                event.getMessage(),
                event.getCount(),
                eventTimestamp(event)
        );
    }

    private static boolean belongsToPod(Event event, String podUid) {
        if (podUid == null || podUid.isBlank()) {
            return true;
        }

        ObjectReference involvedObject = event.getInvolvedObject();
        return involvedObject == null || Objects.equals(podUid, involvedObject.getUid());
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
