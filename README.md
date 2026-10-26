# AuditCore

**Incident Management & Audit Tracking System**

AuditCore is a Java-based incident management project that brings incident records, status updates, and monitoring together through a web dashboard.

## Features
- Incident tracking and management
- Status updates: Open, Investigating, Resolved, and Closed
- Dashboard summaries and incident status filtering
- SQLite database persistence
- REST API for retrieving incidents and updating status
- Automated tests using JUnit 5

## Technology Stack
- Java 21
- Apache Maven
- SQLite
- HTML, CSS, and JavaScript
- Java HTTP Server
- JUnit 5
- Git and GitHub

## Getting Started
### Prerequisites
Install JDK 21 and Apache Maven.

### Clone the repository
```bash
git clone https://github.com/nalagetejas2196-blip/AuditCore.git
cd AuditCore
```

### Build
```bash
mvn compile
```

### Run tests
```bash
mvn test
```

### Dashboard
Start the application using its configured launch method, then open http://127.0.0.1:8080.

## API Endpoints
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/incidents` | Retrieve incident records |
| POST | `/api/incidents/status` | Update an incident status |
| GET | `/` | Open the dashboard |

## Project Scope
AuditCore is a working prototype demonstrating incident tracking, status management, dashboard monitoring, REST API communication, and database persistence. Authentication, role-based access control, comprehensive audit history, advanced analytics, and production hardening are potential future improvements.

## About
Developed by **Tejas Nalage**, a third-year Computer Engineering student.

Repository: https://github.com/nalagetejas2196-blip/AuditCore
