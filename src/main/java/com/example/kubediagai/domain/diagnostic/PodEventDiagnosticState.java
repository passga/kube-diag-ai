package com.example.kubediagai.domain.diagnostic;

public record PodEventDiagnosticState(
        EventAvailability availability,
        String type,
        String reason,
        String message,
        Integer count,
        String timestamp
) {

    public static PodEventDiagnosticState noEventsAvailable() {
        return new PodEventDiagnosticState(EventAvailability.NO_EVENTS_AVAILABLE, null, null, null, null, null);
    }

    public static PodEventDiagnosticState noMatchingEvents() {
        return new PodEventDiagnosticState(EventAvailability.NO_MATCHING_EVENTS, null, null, null, null, null);
    }

    public static PodEventDiagnosticState event(
            String type,
            String reason,
            String message,
            Integer count,
            String timestamp
    ) {
        return new PodEventDiagnosticState(EventAvailability.EVENT, type, reason, message, count, timestamp);
    }

    public enum EventAvailability {
        EVENT,
        NO_EVENTS_AVAILABLE,
        NO_MATCHING_EVENTS
    }
}
