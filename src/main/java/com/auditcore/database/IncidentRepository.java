package com.auditcore.database;

import com.auditcore.Incident;
import com.auditcore.IncidentStatus;
import com.auditcore.Severity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public class IncidentRepository {

    public boolean save(Incident incident) throws SQLException {
        if (incident == null) {
            throw new IllegalArgumentException("Incident cannot be null");
        }

        String sql = """
                INSERT INTO incidents (id, title, description, severity, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, incident.getId());
            statement.setString(2, incident.getTitle());
            statement.setString(3, incident.getDescription());
            statement.setString(4, incident.getSeverity().name());
            statement.setString(5, incident.getStatus().name());
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 19) {
                return false;
            }
            throw exception;
        }
    }

    public List<Incident> findAll() throws SQLException {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT id, title, description, severity, status, created_at FROM incidents ORDER BY created_at, id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Incident incident = new Incident(
                        result.getString("id"),
                        result.getString("title"),
                        result.getString("description"),
                        Severity.valueOf(result.getString("severity")),
                        java.time.LocalDateTime.parse(result.getString("created_at").replace(" ", "T"))
                );
                incident.updateStatus(
                        IncidentStatus.valueOf(result.getString("status"))
                );
                incidents.add(incident);
            }
        }
        return incidents;
    }

    public Optional<Incident> findById(String id) throws SQLException {
        String sql = "SELECT id, title, description, severity, status, created_at FROM incidents WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                Incident incident = new Incident(
                        result.getString("id"),
                        result.getString("title"),
                        result.getString("description"),
                        Severity.valueOf(result.getString("severity")),
                        java.time.LocalDateTime.parse(result.getString("created_at").replace(" ", "T"))
                );
                incident.updateStatus(
                        IncidentStatus.valueOf(result.getString("status"))
                );
                return Optional.of(incident);
            }
        }
    }

    public boolean updateStatus(String id, IncidentStatus status) throws SQLException {
        if (id == null || status == null) {
            return false;
        }

        String sql = "UPDATE incidents SET status = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setString(2, id);
            return statement.executeUpdate() > 0;
        }
    }
}
