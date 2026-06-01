package com.example.kubediagai.domain.diagnostic;

public record PodEventDiagnosticState(
        EventAvailability availability,
        String type,
        String reason,
        String message,
        Integer count,
        String timestamp,
        String errorMessage
) {

    public static PodEventDiagnosticState noEventsAvailable() {
        return new PodEventDiagnosticState(EventAvailability.NO_EVENTS_AVAILABLE, null, null, null, null, null, null);
    }

    public static PodEventDiagnosticState noMatchingEvents() {
        return new PodEventDiagnosticState(EventAvailability.NO_MATCHING_EVENTS, null, null, null, null, null, null);
    }

    public static PodEventDiagnosticState eventsUnavailable(String errorMessage) {
        return new PodEventDiagnosticState(
                EventAvailability.EVENTS_UNAVAILABLE,
                null,
                null,
                null,
                null,
                null,
                errorMessage
        );
    }

    public static PodEventDiagnosticState event(
            String type,
            String reason,
            String message,
            Integer count,
            String timestamp
    ) {
        return new PodEventDiagnosticState(EventAvailability.EVENT, type, reason, message, count, timestamp, null);
    }

    public enum EventAvailability {
        EVENT,
        NO_EVENTS_AVAILABLE,
        NO_MATCHING_EVENTS,
        EVENTS_UNAVAILABLE
    }
}
