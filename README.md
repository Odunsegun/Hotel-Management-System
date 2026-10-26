# Grand Horizon Hotel Management System

A demo-ready hotel management web application built with Spring Boot. Manage customers, rooms, services, bookings, and invoices — all from a polished staff portal with login and dashboard.

## Features

- **Secure login** with admin and staff roles
- **Dashboard** with occupancy stats and recent bookings
- **Customer, room, and service management** (full CRUD)
- **Booking management** with date validation and room availability checks
- **Invoice generation** with room charges and add-on services
- **Pre-loaded demo data** so the app looks populated on first launch

## Tech Stack

- Java 17
- Spring Boot 3.2 (Web, JPA, Security, Thymeleaf, Validation)
- H2 database: persistent by default, in-memory in demo mode
- HTML/CSS frontend

## Prerequisites

- Java JDK 17+
- Maven (or use the included `mvnw` wrapper)

## Quick Start

```bash
cd HotelManagementSystemFinalProject
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

On Windows:

```bash
cd HotelManagementSystemFinalProject
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

## Demo Credentials

| Role  | Username | Password  |
|-------|----------|-----------|
| Admin | admin    | admin123  |
| Staff | staff    | staff123  |

## Demo Walkthrough

1. Log in with admin credentials
2. View the dashboard (room count, active bookings, check-ins/outs)
3. Browse customers, rooms, and services (pre-loaded sample data)
4. Create a new booking — select customer, room, dates, and optional services
5. Try booking the same room on overlapping dates (validation will reject it)
6. Open a booking invoice to see the itemized bill

## Project Structure

```
HotelManagementSystemFinalProject/
├── src/main/java/com/example/demo/
│   ├── config/          Security configuration
│   ├── controller/      Web controllers
│   ├── dto/             Dashboard data transfer objects
│   ├── model/           JPA entities
│   ├── repository/      Data access layer
│   └── service/         Business logic
├── src/main/resources/
│   ├── templates/       Thymeleaf HTML pages
│   ├── static/css/      Stylesheet
│   ├── data.sql         Demo seed data
│   └── application.properties
└── pom.xml
```

## Notes

- In demo mode, data is stored in memory and resets on each restart.
- In demo mode, the H2 console is available at `/h2-console` (JDBC URL: `jdbc:h2:mem:hotel_db`, user: `sa`, no password).
- Payment gateway integration is listed as a future improvement.

## License

MIT License

## Persistent local mode

Without the demo profile, data is stored in a file database and survives restarts. Set HOTEL_ADMIN_PASSWORD and HOTEL_STAFF_PASSWORD before starting. The H2 console and demo seed data are disabled. Staff can manage guests and bookings; only admins can change rooms/services or delete customers. Use JDK 17 for this Spring Boot 3.2 project.

Booking prices are captured when booked. Existing rates survive catalog price edits; changing a booking's room uses that room's current rate, and newly added services use their current prices.

Run tests with .\mvnw.cmd test from the application directory. The demo profile uses disposable data and public sample passwords; do not deploy it publicly.
