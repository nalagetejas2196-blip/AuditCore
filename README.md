# AuditCore

A Java-based incident management and audit tracking application.

## Features
- Incident creation and validation
- Duplicate ID prevention and incident search
- Incident status updates
- Severity filtering and statistics
- Unresolved incident counting
- Dashboard summary statistics
- Automated JUnit 5 tests

## Technology Stack
- Java 21
- Apache Maven
- JUnit 5
- Git

## Run the Application
```bash
mvn clean compile
java -cp target/classes com.auditcore.AuditCoreApplication
```

## Run Tests
```bash
mvn clean test
```

## Current Scope
Incidents are stored in memory and are not retained after the application exits. Database persistence, a web interface, and REST APIs are planned enhancements.
