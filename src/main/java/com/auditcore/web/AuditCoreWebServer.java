package com.auditcore.web;

import com.auditcore.Incident;
import com.auditcore.database.DatabaseManager;
import com.auditcore.database.IncidentRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class AuditCoreWebServer {

    private static final IncidentRepository repository = new IncidentRepository();

    public static void main(String[] args) throws IOException {
        try {
            DatabaseManager.initializeDatabase();
        } catch (SQLException exception) {
            throw new IOException("Could not initialize the AuditCore database.", exception);
        }

        int port = 8080;
        Path dashboard = Path.of("src", "main", "resources", "static", "index.html");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);

        server.createContext("/api/incidents", exchange -> {
            try {
                if (!exchange.getRequestMethod().equals("GET")) {
                    send(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    return;
                }

                StringBuilder json = new StringBuilder("[");
                boolean first = true;

                for (Incident incident : repository.findAll()) {
                    if (!first) {
                        json.append(",");
                    }
                    first = false;

                    json.append("{")
                        .append("\"id\":\"").append(escape(incident.getId())).append("\",")
                        .append("\"title\":\"").append(escape(incident.getTitle())).append("\",")
                        .append("\"description\":\"").append(escape(incident.getDescription())).append("\",")
                        .append("\"severity\":\"").append(incident.getSeverity().name()).append("\",")
                        .append("\"status\":\"").append(incident.getStatus().name()).append("\",")
                        .append("\"createdAt\":\"").append(incident.getCreatedAt() == null ? "" : incident.getCreatedAt().toString()).append("\"")
                        .append("}");
                }

                json.append("]");
                send(exchange, 200, json.toString());
            } catch (SQLException exception) {
                exception.printStackTrace();
                send(exchange, 500, "{\"error\":\"Unable to load incidents\"}");
            } catch (IOException exception) {
                exception.printStackTrace();
                exchange.close();
            }
        });

        server.createContext("/api/incidents/status", exchange -> {
            try {
                if (!exchange.getRequestMethod().equals("POST")) {
                    send(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    return;
                }

                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String id = null;
                String statusValue = null;
                java.util.regex.Matcher idMatcher = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
                java.util.regex.Matcher statusMatcher = java.util.regex.Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"").matcher(body);

                if (idMatcher.find()) id = idMatcher.group(1);
                if (statusMatcher.find()) statusValue = statusMatcher.group(1);

                if (id == null || statusValue == null) {
                    send(exchange, 400, "{\"error\":\"Provide id and status\"}");
                    return;
                }

                com.auditcore.IncidentStatus status;
                try {
                    status = com.auditcore.IncidentStatus.valueOf(statusValue);
                } catch (IllegalArgumentException exception) {
                    send(exchange, 400, "{\"error\":\"Invalid status\"}");
                    return;
                }

                if (!repository.updateStatus(id, status)) {
                    send(exchange, 404, "{\"error\":\"Incident not found\"}");
                    return;
                }

                send(exchange, 200, "{\"message\":\"Incident status updated\",\"id\":\"" + escape(id) + "\",\"status\":\"" + status.name() + "\"}");
            } catch (SQLException exception) {
                exception.printStackTrace();
                send(exchange, 500, "{\"error\":\"Unable to update incident status\"}");
            } catch (IOException exception) {
                exception.printStackTrace();
                exchange.close();
            }
        });

        server.createContext("/", exchange -> {
            try {
                if (!exchange.getRequestMethod().equals("GET")) {
                    send(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    return;
                }

                if (!exchange.getRequestURI().getPath().equals("/")) {
                    send(exchange, 404, "{\"error\":\"Not found\"}");
                    return;
                }

                byte[] response = Files.readAllBytes(dashboard);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
                exchange.sendResponseHeaders(200, response.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                exchange.close();
            }
        });

        server.start();
        System.out.println("AuditCore web server started.");
        System.out.println("Open http://127.0.0.1:" + port);
        System.out.println("Incidents API: http://127.0.0.1:" + port + "/api/incidents");
        System.out.println("Press Ctrl+C to stop the server.");

        try {
            new java.util.concurrent.CountDownLatch(1).await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);

        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder();

        for (char character : value.toCharArray()) {
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }

        return escaped.toString();
    }
}
