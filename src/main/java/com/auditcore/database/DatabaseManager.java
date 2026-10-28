package com.auditcore.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:auditcore.db";

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initializeDatabase() throws SQLException {
        String incidents = """
                CREATE TABLE IF NOT EXISTS incidents (
                    id TEXT PRIMARY KEY COLLATE NOCASE,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    severity TEXT NOT NULL CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
                    status TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','INVESTIGATING','RESOLVED','CLOSED')),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String audit = """
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    incident_id TEXT NOT NULL,
                    action TEXT NOT NULL,
                    old_status TEXT,
                    new_status TEXT NOT NULL,
                    actor TEXT NOT NULL DEFAULT 'local-user',
                    details TEXT,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(incidents);
            statement.execute(audit);
        }
    }
}
