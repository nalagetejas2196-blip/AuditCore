package com.auditcore.database;

import com.auditcore.Incident;
import com.auditcore.IncidentStatus;
import com.auditcore.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class IncidentRepositoryTest {

    private IncidentRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.initializeDatabase();

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM incidents");
        }

        repository = new IncidentRepository();
    }

    @Test
    void savesAndFindsIncidentById() throws Exception {
        Incident incident = new Incident(
                "TEST-001", "Login failure", "Repeated failed login attempts", Severity.HIGH);

        assertTrue(repository.save(incident));

        var found = repository.findById("test-001");
        assertTrue(found.isPresent());
        assertEquals("TEST-001", found.get().getId());
        assertEquals(Severity.HIGH, found.get().getSeverity());
    }

    @Test
    void findsAllSavedIncidents() throws Exception {
        repository.save(new Incident("TEST-002", "Server alert", "Server is down", Severity.CRITICAL));
        repository.save(new Incident("TEST-003", "Access review", "Review access permissions", Severity.LOW));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void updatesIncidentStatus() throws Exception {
        repository.save(new Incident("TEST-004", "Audit alert", "Unexpected activity", Severity.MEDIUM));

        assertTrue(repository.updateStatus("TEST-004", IncidentStatus.INVESTIGATING));
        assertEquals(IncidentStatus.INVESTIGATING,
                repository.findById("TEST-004").orElseThrow().getStatus());
    }

    @Test
    void rejectsDuplicateIncidentIds() throws Exception {
        Incident first = new Incident("TEST-005", "First", "First incident", Severity.LOW);
        Incident duplicate = new Incident("test-005", "Duplicate", "Duplicate incident", Severity.HIGH);

        assertTrue(repository.save(first));
        assertFalse(repository.save(duplicate));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void returnsEmptyWhenIncidentDoesNotExist() throws Exception {
        assertTrue(repository.findById("MISSING-001").isEmpty());
    }
}