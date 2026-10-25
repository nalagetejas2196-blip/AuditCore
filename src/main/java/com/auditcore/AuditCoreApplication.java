package com.auditcore;

public class AuditCoreApplication {

    public static void main(String[] args) {
        IncidentManager manager = new IncidentManager();

        manager.addIncident(new Incident(
            "INC-1001",
            "Suspicious Login Activity",
            "Multiple failed login attempts detected.",
            Severity.HIGH
        ));

        manager.addIncident(new Incident(
            "INC-1002",
            "Unusual Network Traffic",
            "Unexpected outbound traffic detected.",
            Severity.MEDIUM
        ));

        manager.addIncident(new Incident(
            "INC-1003",
            "Unauthorized Access Attempt",
            "Access to a restricted resource was denied.",
            Severity.CRITICAL
        ));

        System.out.println("========== AUDITCORE ==========");
        System.out.println("Total incidents: " + manager.getIncidentCount());

        for (Incident incident : manager.getAllIncidents()) {
            System.out.println(
                incident.getId() + " | " +
                incident.getSeverity() + " | " +
                incident.getTitle() + " | " +
                incident.getStatus()
            );
        }

        Incident found = manager.findById("INC-1002");
        System.out.println("\nSearch result:");
        System.out.println(found == null ? "Incident not found." : found.getTitle());

        found.updateStatus(IncidentStatus.INVESTIGATING);
        System.out.println("Updated status: " + found.getStatus());
    }
}