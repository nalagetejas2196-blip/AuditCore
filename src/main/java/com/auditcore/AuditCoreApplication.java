package com.auditcore;

import com.auditcore.database.DatabaseManager;
import com.auditcore.database.IncidentRepository;

import java.sql.SQLException;

public class AuditCoreApplication {

    public static void main(String[] args) {
        try {
            DatabaseManager.initializeDatabase();
            IncidentRepository repository = new IncidentRepository();

            saveIfMissing(repository, new Incident(
                    "INC-1001", "Suspicious Login Activity",
                    "Multiple failed login attempts detected.", Severity.HIGH));

            saveIfMissing(repository, new Incident(
                    "INC-1002", "Unusual Network Traffic",
                    "Unexpected outbound traffic detected.", Severity.MEDIUM));

            saveIfMissing(repository, new Incident(
                    "INC-1003", "Unauthorized Access Attempt",
                    "Access to a restricted resource was denied.", Severity.CRITICAL));

            System.out.println("========== AUDITCORE ==========");
            System.out.println("Total incidents: " + repository.findAll().size());

            for (Incident incident : repository.findAll()) {
                System.out.println(
                        incident.getId() + " | " +
                        incident.getSeverity() + " | " +
                        incident.getTitle() + " | " +
                        incident.getStatus()
                );
            }

            Incident found = repository.findById("INC-1002").orElse(null);
            System.out.println("\nSearch result:");
            System.out.println(found == null ? "Incident not found." : found.getTitle());

            if (found != null &&
                    repository.updateStatus(found.getId(), IncidentStatus.INVESTIGATING)) {
                System.out.println("Updated status: " +
                        repository.findById(found.getId()).orElseThrow().getStatus());
            }

        } catch (SQLException exception) {
            System.err.println("AuditCore database error: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void saveIfMissing(
            IncidentRepository repository, Incident incident) throws SQLException {
        if (repository.findById(incident.getId()).isEmpty()) {
            repository.save(incident);
        }
    }
}