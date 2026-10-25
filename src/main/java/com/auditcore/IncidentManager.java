package com.auditcore;

public class IncidentManager {
    private final Incident[] incidents = new Incident[100];
    private int count = 0;

    public boolean addIncident(Incident incident) {
        if (incident == null || count >= incidents.length) {
            return false;
        }

        if (findById(incident.getId()) != null) {
            return false;
        }

        incidents[count] = incident;
        count++;
        return true;
    }

    public Incident[] getAllIncidents() {
        Incident[] result = new Incident[count];

        for (int i = 0; i < count; i++) {
            result[i] = incidents[i];
        }

        return result;
    }

    public Incident[] getIncidentsBySeverity(Severity severity) {
        if (severity == null) {
            return new Incident[0];
        }

        int matches = 0;

        for (int i = 0; i < count; i++) {
            if (incidents[i].getSeverity() == severity) {
                matches++;
            }
        }

        Incident[] result = new Incident[matches];
        int index = 0;

        for (int i = 0; i < count; i++) {
            if (incidents[i].getSeverity() == severity) {
                result[index++] = incidents[i];
            }
        }

        return result;
    }

    public int countBySeverity(Severity severity) {
        if (severity == null) {
            return 0;
        }

        int total = 0;

        for (int i = 0; i < count; i++) {
            if (incidents[i].getSeverity() == severity) {
                total++;
            }
        }

        return total;
    }
    public int countByStatus(IncidentStatus status) {
        if (status == null) {
            return 0;
        }

        int total = 0;

        for (int i = 0; i < count; i++) {
            if (incidents[i].getStatus() == status) {
                total++;
            }
        }

        return total;
    }
    public int countUnresolvedIncidents() {
        int total = 0;

        for (int i = 0; i < count; i++) {
            IncidentStatus status = incidents[i].getStatus();

            if (status != IncidentStatus.RESOLVED
                    && status != IncidentStatus.CLOSED) {
                total++;
            }
        }

        return total;
    }
    public String getDashboardSummary() {
        return "AUDITCORE DASHBOARD" + System.lineSeparator()
            + "Total incidents: " + getIncidentCount() + System.lineSeparator()
            + "Unresolved incidents: " + countUnresolvedIncidents() + System.lineSeparator()
            + "Critical incidents: " + countBySeverity(Severity.CRITICAL) + System.lineSeparator()
            + "High-severity incidents: " + countBySeverity(Severity.HIGH) + System.lineSeparator()
            + "Open incidents: " + countByStatus(IncidentStatus.OPEN) + System.lineSeparator()
            + "Investigating incidents: " + countByStatus(IncidentStatus.INVESTIGATING) + System.lineSeparator()
            + "Resolved incidents: " + countByStatus(IncidentStatus.RESOLVED) + System.lineSeparator()
            + "Closed incidents: " + countByStatus(IncidentStatus.CLOSED);
    }
    public int getIncidentCount() {
        return count;
    }

    public Incident findById(String id) {
        if (id == null) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            if (incidents[i].getId().equalsIgnoreCase(id)) {
                return incidents[i];
            }
        }

        return null;
    }

    public boolean updateIncidentStatus(String id, IncidentStatus status) {
        Incident incident = findById(id);

        if (incident == null || status == null) {
            return false;
        }

        incident.updateStatus(status);
        return true;
    }
}