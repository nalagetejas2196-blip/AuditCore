package com.auditcore;

public class Incident {
    private final String id;
    private final String title;
    private final String description;
    private final Severity severity;
    private IncidentStatus status;

    public Incident(String id, String title, String description, Severity severity) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Incident ID cannot be blank.");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Incident title cannot be blank.");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Incident description cannot be blank.");
        }

        if (severity == null) {
            throw new IllegalArgumentException("Incident severity is required.");
        }

        this.id = id.trim();
        this.title = title.trim();
        this.description = description.trim();
        this.severity = severity;
        this.status = IncidentStatus.OPEN;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void updateStatus(IncidentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Incident status is required.");
        }

        this.status = status;
    }
}