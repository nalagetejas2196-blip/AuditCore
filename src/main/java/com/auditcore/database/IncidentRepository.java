package com.auditcore.database;

import com.auditcore.IncidentStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IncidentRepository {

    public boolean save(com.auditcore.Incident incident) throws SQLException {
        if (incident == null) throw new IllegalArgumentException("Incident cannot be null");
        String sql = "INSERT INTO incidents (id,title,description,severity,status) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, incident.getId());
            s.setString(2, incident.getTitle());
            s.setString(3, incident.getDescription());
            s.setString(4, incident.getSeverity().name());
            s.setString(5, incident.getStatus().name());
            s.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) return false;
            throw e;
        }
    }

    public List<com.auditcore.Incident> findAll() throws SQLException {
        List<com.auditcore.Incident> items = new ArrayList<>();
        String sql = "SELECT id,title,description,severity,status,created_at FROM incidents ORDER BY created_at,id";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql); ResultSet r = s.executeQuery()) {
            while (r.next()) items.add(readIncident(r));
        }
        return items;
    }

    public java.util.Optional<com.auditcore.Incident> findById(String id) throws SQLException {
        String sql = "SELECT id,title,description,severity,status,created_at FROM incidents WHERE id=?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, id);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? java.util.Optional.of(readIncident(r)) : java.util.Optional.empty();
            }
        }
    }

    public boolean updateStatus(String id, IncidentStatus status) throws SQLException {
        if (id == null || status == null) return false;
        try (Connection c = DatabaseManager.getConnection()) {
            c.setAutoCommit(false);
            try {
                String oldStatus = null;
                try (PreparedStatement s = c.prepareStatement("SELECT status FROM incidents WHERE id=?")) {
                    s.setString(1, id);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            return false;
                        }
                        oldStatus = r.getString("status");
                    }
                }

                try (PreparedStatement s = c.prepareStatement("UPDATE incidents SET status=? WHERE id=?")) {
                    s.setString(1, status.name());
                    s.setString(2, id);
                    s.executeUpdate();
                }

                if (!oldStatus.equals(status.name())) {
                    try (PreparedStatement s = c.prepareStatement(
                            "INSERT INTO audit_logs (incident_id,action,old_status,new_status,actor,details) VALUES (?,?,?,?,?,?)")) {
                        s.setString(1, id);
                        s.setString(2, "STATUS_CHANGED");
                        s.setString(3, oldStatus);
                        s.setString(4, status.name());
                        s.setString(5, "local-user");
                        s.setString(6, "Incident status updated");
                        s.executeUpdate();
                    }
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public List<String[]> findAuditLogs() throws SQLException {
        List<String[]> logs = new ArrayList<>();
        String sql = "SELECT id,incident_id,action,old_status,new_status,actor,details,created_at FROM audit_logs ORDER BY id DESC";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql); ResultSet r = s.executeQuery()) {
            while (r.next()) logs.add(new String[]{
                r.getString("id"), r.getString("incident_id"), r.getString("action"),
                r.getString("old_status"), r.getString("new_status"), r.getString("actor"),
                r.getString("details"), r.getString("created_at")
            });
        }
        return logs;
    }

    private com.auditcore.Incident readIncident(ResultSet r) throws SQLException {
        com.auditcore.Incident incident = new com.auditcore.Incident(
            r.getString("id"), r.getString("title"), r.getString("description"),
            com.auditcore.Severity.valueOf(r.getString("severity")),
            java.time.LocalDateTime.parse(r.getString("created_at").replace(" ", "T"))
        );
        incident.updateStatus(IncidentStatus.valueOf(r.getString("status")));
        return incident;
    }
}
