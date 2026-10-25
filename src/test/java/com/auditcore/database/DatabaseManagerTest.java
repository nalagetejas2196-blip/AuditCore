package com.auditcore.database;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    @Test
    void initializesDatabaseAndCreatesIncidentsTable() throws Exception {
        DatabaseManager.initializeDatabase();

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='incidents'")) {
            assertTrue(result.next(), "Incidents table should exist");
        }
    }
}