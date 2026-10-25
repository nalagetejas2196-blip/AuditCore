package com.auditcore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IncidentManagerTest {

    @Test
    void shouldAddAndFindIncident() {
        IncidentManager manager = new IncidentManager();
        Incident incident = new Incident(
            "INC-4001", "Suspicious Login",
            "Repeated failed login attempts.", Severity.HIGH
        );

        assertTrue(manager.addIncident(incident));
        assertEquals(1, manager.getIncidentCount());
        assertEquals("INC-4001", manager.findById("inc-4001").getId());
    }

    @Test
    void shouldRejectDuplicateIncidentIds() {
        IncidentManager manager = new IncidentManager();

        assertTrue(manager.addIncident(
            new Incident("INC-4002", "First", "First incident.", Severity.MEDIUM)
        ));

        assertFalse(manager.addIncident(
            new Incident("INC-4002", "Duplicate", "Duplicate incident.", Severity.LOW)
        ));

        assertEquals(1, manager.getIncidentCount());
    }

    @Test
    void shouldUpdateIncidentStatus() {
        IncidentManager manager = new IncidentManager();
        manager.addIncident(
            new Incident("INC-4003", "Network Alert", "Unusual traffic.", Severity.CRITICAL)
        );

        assertTrue(manager.updateIncidentStatus(
            "INC-4003", IncidentStatus.INVESTIGATING
        ));

        assertEquals(
            IncidentStatus.INVESTIGATING,
            manager.findById("INC-4003").getStatus()
        );
    }

    @Test
    void shouldRejectMissingIncidentAndNullStatus() {
        IncidentManager manager = new IncidentManager();

        assertFalse(manager.updateIncidentStatus(
            "INC-9999", IncidentStatus.RESOLVED
        ));

        assertFalse(manager.updateIncidentStatus("INC-9999", null));
    }

    @Test
    void shouldRejectInvalidIncidentData() {
        assertThrows(IllegalArgumentException.class, () ->
            new Incident("", "Title", "Description", Severity.LOW)
        );

        assertThrows(IllegalArgumentException.class, () ->
            new Incident("INC-4004", " ", "Description", Severity.LOW)
        );

        assertThrows(IllegalArgumentException.class, () ->
            new Incident("INC-4005", "Title", " ", Severity.LOW)
        );

        assertThrows(IllegalArgumentException.class, () ->
            new Incident("INC-4006", "Title", "Description", null)
        );
    }

    @Test
    void shouldRejectIncidentWhenCapacityIsReached() {
        IncidentManager manager = new IncidentManager();

        for (int i = 1; i <= 100; i++) {
            assertTrue(manager.addIncident(
                new Incident(
                    "INC-" + i,
                    "Incident " + i,
                    "Test incident " + i,
                    Severity.LOW
                )
            ));
        }

        assertEquals(100, manager.getIncidentCount());

        assertFalse(manager.addIncident(
            new Incident(
                "INC-101",
                "Overflow Incident",
                "This should not be accepted.",
                Severity.HIGH
            )
        ));

        assertEquals(100, manager.getIncidentCount());
    }

    @Test
    void shouldFilterIncidentsBySeverity() {
        IncidentManager manager = new IncidentManager();

        manager.addIncident(new Incident(
            "INC-5001", "Login Alert", "Repeated login failures.", Severity.HIGH
        ));
        manager.addIncident(new Incident(
            "INC-5002", "Traffic Alert", "Unusual network traffic.", Severity.MEDIUM
        ));
        manager.addIncident(new Incident(
            "INC-5003", "Access Alert", "Critical access attempt.", Severity.HIGH
        ));

        Incident[] highSeverity = manager.getIncidentsBySeverity(Severity.HIGH);

        assertEquals(2, highSeverity.length);
        assertEquals(Severity.HIGH, highSeverity[0].getSeverity());
        assertEquals(Severity.HIGH, highSeverity[1].getSeverity());

        assertEquals(0, manager.getIncidentsBySeverity(null).length);
        assertEquals(0, manager.getIncidentsBySeverity(Severity.CRITICAL).length);
    }

    @Test
    void shouldCountIncidentsBySeverity() {
        IncidentManager manager = new IncidentManager();

        manager.addIncident(new Incident(
            "INC-6001", "Login Alert", "Repeated login failures.", Severity.HIGH
        ));
        manager.addIncident(new Incident(
            "INC-6002", "Traffic Alert", "Unusual network traffic.", Severity.MEDIUM
        ));
        manager.addIncident(new Incident(
            "INC-6003", "Access Alert", "Suspicious access attempt.", Severity.HIGH
        ));

        assertEquals(2, manager.countBySeverity(Severity.HIGH));
        assertEquals(1, manager.countBySeverity(Severity.MEDIUM));
        assertEquals(0, manager.countBySeverity(Severity.LOW));
        assertEquals(0, manager.countBySeverity(null));
    }

    @Test
    void shouldCountIncidentsByStatus() {
        IncidentManager manager = new IncidentManager();

        manager.addIncident(new Incident(
            "INC-7001", "Login Alert", "Repeated login failures.", Severity.HIGH
        ));
        manager.addIncident(new Incident(
            "INC-7002", "Network Alert", "Unusual network traffic.", Severity.MEDIUM
        ));

        manager.updateIncidentStatus("INC-7001", IncidentStatus.INVESTIGATING);
        manager.updateIncidentStatus("INC-7002", IncidentStatus.RESOLVED);

        assertEquals(0, manager.countByStatus(IncidentStatus.OPEN));
        assertEquals(1, manager.countByStatus(IncidentStatus.INVESTIGATING));
        assertEquals(1, manager.countByStatus(IncidentStatus.RESOLVED));
        assertEquals(0, manager.countByStatus(IncidentStatus.CLOSED));
        assertEquals(0, manager.countByStatus(null));
    }

    @Test
    void shouldCountUnresolvedIncidents() {
        IncidentManager manager = new IncidentManager();

        manager.addIncident(new Incident(
            "INC-8001", "Open Alert", "An open incident.", Severity.LOW
        ));
        manager.addIncident(new Incident(
            "INC-8002", "Investigation Alert", "Under investigation.", Severity.HIGH
        ));
        manager.addIncident(new Incident(
            "INC-8003", "Resolved Alert", "Already resolved.", Severity.MEDIUM
        ));
        manager.addIncident(new Incident(
            "INC-8004", "Closed Alert", "Already closed.", Severity.LOW
        ));

        manager.updateIncidentStatus("INC-8002", IncidentStatus.INVESTIGATING);
        manager.updateIncidentStatus("INC-8003", IncidentStatus.RESOLVED);
        manager.updateIncidentStatus("INC-8004", IncidentStatus.CLOSED);

        assertEquals(2, manager.countUnresolvedIncidents());

        manager.updateIncidentStatus("INC-8001", IncidentStatus.RESOLVED);
        assertEquals(1, manager.countUnresolvedIncidents());
    }

    @Test
    void shouldGenerateDashboardSummary() {
        IncidentManager manager = new IncidentManager();

        manager.addIncident(new Incident(
            "INC-9001", "Critical Alert", "Critical security event.", Severity.CRITICAL
        ));
        manager.addIncident(new Incident(
            "INC-9002", "Login Alert", "Repeated login failures.", Severity.HIGH
        ));

        manager.updateIncidentStatus("INC-9002", IncidentStatus.INVESTIGATING);

        String summary = manager.getDashboardSummary();

        assertTrue(summary.contains("Total incidents: 2"));
        assertTrue(summary.contains("Unresolved incidents: 2"));
        assertTrue(summary.contains("Critical incidents: 1"));
        assertTrue(summary.contains("High-severity incidents: 1"));
        assertTrue(summary.contains("Open incidents: 1"));
        assertTrue(summary.contains("Investigating incidents: 1"));
        assertTrue(summary.contains("Resolved incidents: 0"));
        assertTrue(summary.contains("Closed incidents: 0"));
    }
}
