# BrightCare Medical Centre (Java RMI)

Distributed clinic management system for BrightCare Medical Centre built with Java RMI, Java Swing, and embedded Apache Derby.

## System overview

The system has 1 RMI server and 4 client roles:

1. **Receptionist**: register/search/update/delete patient records.
2. **Patient**: view profile, check availability, book appointments, cancel appointments, view schedules/history.
3. **Doctor**: manage pending appointments, reschedule/cancel, manage weekly slots, update consultation notes, view patient history, update profile/settings.
4. **Admin**: user management, audit log viewing, report generation.

## Project structure

- Active source path: `src\brigthcare_medical_centre\...`
- Active test path: `test\brigthcare_medical_centre\tests\...`
- Build output: `build\classes`

> The nested folder `BrigthCare_Medical_Centre\` is a legacy duplicate and is **not** the active build source.

## Completed implementation and updates

### Core features completed

- RMI server startup and service binding for Authentication, Admin, Report, Patient, Doctor, Receptionist.
- Centralized authentication with role-based access checks.
- Embedded Derby schema setup and default seed users.
- Full Swing clients for all roles.
- Admin reporting module (monthly appointments, doctor consultations, patient visits).
- Audit logging for system actions.

### Critical fixes completed

- **Appointment integrity hardening**
  - Booking is now transaction-safe to prevent duplicate slot reservation.
  - Appointment state transitions are validated (pending/accepted/completed/cancelled/rejected).
  - Slot availability and appointment status are synchronized.

- **Known doctor slot bug fixed**
  - Doctor weekly add/remove slot actions no longer override occupied slots.
  - UI now reports partial updates when some dates are blocked by active bookings.

- **Patient cancellation safety**
  - Cancellation now requires the logged-in username to prevent cross-user cancellation.
  - Patients can cancel both **PENDING** and **ACCEPTED** appointments.

- **Doctor appointment management completed**
  - Added doctor-side **Manage Appointments** tab with cancel/reschedule workflows.

- **Admin provisioning consistency**
  - Admin-created users are now provisioned in role-specific tables (`DOCTORS`, `PATIENTS`, `RECEPTIONISTS`).
  - Role changes and user deletion are validated against dependent data to protect integrity.

- **Report schema alignment**
  - Report queries now match actual Derby schema (`APPOINTMENTS`, `DOCTORS`, `PATIENTS`, `CONSULTATION_NOTES`).

- **Database reliability**
  - Replaced shared singleton DB connection with short-lived per-call connections for safer concurrent RMI operations.

- **SSL wiring**
  - SSL-enabled RMI is now supported behind config flags.
  - Server validates required keystore/truststore properties when SSL is enabled.

- **Audit logging fix**
  - System-level actions now log with nullable user IDs instead of invalid FK values.

## Prerequisites

1. Windows OS (batch scripts are provided for Windows).
2. JDK 8 installed and available on `PATH` (`java -version`, `javac -version`).
3. `lib\derby.jar` present in the repository.

## Default seeded accounts

- Admin: `admin` / `admin123`
- Doctor: `doctor1` / `doctor123`
- Patient: `patient1` / `patient123`
- Receptionist: `receptionist1` / `receptionist123`

## How to run (recommended)

### 1. Build the project

Run from repository root:

```bat
build.bat
```

### 2. Start the server (keep this terminal open)

```bat
start_server.bat
```

### 3. Start client apps in separate terminals

Admin:

```bat
start_admin.bat
```

Doctor:

```bat
start_doctor.bat
```

Patient:

```bat
start_patient.bat
```

Receptionist:

```bat
start_receptionist.bat
```

## SSL mode (optional)

SSL is disabled by default. To enable:

1. Set JVM property `-Dbrightcare.ssl.enabled=true`
2. Provide all SSL properties:
   - `-Djavax.net.ssl.keyStore=<path>`
   - `-Djavax.net.ssl.keyStorePassword=<password>`
   - `-Djavax.net.ssl.trustStore=<path>`
   - `-Djavax.net.ssl.trustStorePassword=<password>`

If SSL is enabled but these properties are missing, server startup will fail fast with a clear error.

## Testing

### Regression smoke tests

Run from repository root:

```bat
run_tests.bat
```

This compiles and runs:

- `test\brigthcare_medical_centre\tests\RegressionSmokeTests.java`

Covered scenarios:

1. booking integrity
2. patient cancellation ownership and slot restore
3. doctor reschedule and slot guardrails
4. admin role provisioning and cleanup
5. report generation compatibility with actual schema

## Troubleshooting

- **Client cannot connect to server**: ensure `start_server.bat` is running first.
- **Build fails**: verify JDK 8 and `lib\derby.jar`.
- **Port conflict (1099)**: stop other RMI services or change RMI port via JVM property `-Dbrightcare.rmi.port=<port>`.
- **Wrong files being edited**: only edit under root `src\...`, not nested legacy project folder.

## Notes for contributors

- Keep schema-related changes synchronized with:
  - `src\brigthcare_medical_centre\database\DatabaseSetup.java`
  - `src\brigthcare_medical_centre\report\ReportGenerator.java`
  - appointment/schedule logic in `PatientDB` and `DoctorDB`
- Prefer running `build.bat` and `run_tests.bat` before pushing.
