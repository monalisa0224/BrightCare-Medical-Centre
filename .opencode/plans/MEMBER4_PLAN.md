# Member 4 — Admin + Server Implementation Plan

## Overview

Member 4 is responsible for the **central RMI server infrastructure** and the **Admin module**. This includes setting up the RMI registry, handling concurrent connections, providing authentication for all roles, generating reports, and building a simple Swing GUI for the clinic administrator.

---

## File Structure

```
src/brigthcare_medical_centre/
│
├── BrigthCare_Medical_Centre.java      // Project entry point (launches server or admin client)
│
├── server/
│   ├── ServerDriver.java               // Entry point: starts RMI registry & binds remote objects
│   └── RmiServer.java                  // Initializes services (Authentication, Admin, Report)
│
├── common/                             // Remote interfaces (shared contract across all members)
│   ├── AdminInterface.java             // Remote: admin login, manage reports, view logs
│   ├── ReportInterface.java            // Remote: generate & retrieve reports
│   └── AuthenticationInterface.java    // Remote: unified login for all roles
│
├── admin/
│   ├── AdminImpl.java                  // Remote object implementing AdminInterface
│   ├── AdminService.java               // Business logic for admin operations
│   └── Admin.java                      // Entity (POJO)
│
├── report/
│   ├── ReportImpl.java                 // Remote object implementing ReportInterface
│   ├── ReportGenerator.java            // SQL queries + data aggregation
│   ├── Report.java                     // Entity (POJO)
│   └── ReportType.java                 // Enum: MONTHLY_APPOINTMENTS, DOCTOR_CONSULTATIONS, PATIENT_VISITS
│
├── auth/
│   ├── AuthenticationImpl.java         // Remote object implementing AuthenticationInterface
│   ├── User.java                       // Entity for USERS table
│   └── UserRole.java                   // Enum: ADMIN, DOCTOR, RECEPTIONIST, PATIENT
│
├── database/
│   ├── DerbyConnection.java            // Singleton connection manager (Apache Derby)
│   ├── DatabaseSetup.java              // Creates all tables & pre-seeds default admin
│   └── AuditLogger.java                // Thread-safe audit logging utility
│
├── gui/admin/
│   ├── AdminLoginFrame.java            // Login window
│   ├── AdminDashboardFrame.java        // Main dashboard with tabbed navigation
│   ├── ReportPanel.java                // Form: select report type + date range
│   ├── ReportResultPanel.java          // Display generated report in a table
│   └── LogViewerPanel.java             // Browse system audit logs
│
└── util/
    ├── SslUtil.java                    // SSL/TLS setup utilities
    ├── DateUtils.java                  // Date formatting & parsing helpers
    └── Constants.java                  // RMI port, DB URL, shared constants
```

---

## Database Design (Apache Derby)

### Tables Owned by Member 4

#### USERS table (shared by all roles)

| Column       | Type         | Constraints              |
|-------------|--------------|--------------------------|
| UserID      | INT          | PRIMARY KEY, GENERATED ALWAYS AS IDENTITY |
| Username    | VARCHAR(50)  | UNIQUE, NOT NULL         |
| PasswordHash| VARCHAR(256) | NOT NULL                 |
| Role        | VARCHAR(20)  | NOT NULL                 |
| CreatedDate | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP|

#### LOGS table (shared audit trail)

| Column    | Type         | Constraints              |
|-----------|--------------|--------------------------|
| LogID     | INT          | PRIMARY KEY, GENERATED ALWAYS AS IDENTITY |
| UserID    | INT          | FOREIGN KEY → USERS(UserID) |
| Action    | VARCHAR(100) | NOT NULL                 |
| Timestamp | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP|
| Details   | VARCHAR(500) |                          |

#### REPORTS table

| Column       | Type         | Constraints              |
|-------------|--------------|--------------------------|
| ReportID    | INT          | PRIMARY KEY, GENERATED ALWAYS AS IDENTITY |
| AdminID     | INT          | FOREIGN KEY → USERS(UserID) |
| ReportType  | VARCHAR(50)  | NOT NULL                 |
| GeneratedDate | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP|
| Parameters  | VARCHAR(200) | (e.g. date range JSON)   |
| ResultData  | CLOB         | (stored report data as JSON/CSV) |

### Pre-seeded Admin Account

```
Username: admin
Password: admin123
Role: ADMIN
```

> This is pre-seeded in `DatabaseSetup.java` for first-time access.

---

## RMI Architecture

### Remote Interfaces

```java
// AuthenticationInterface.java
public interface AuthenticationInterface extends Remote {
    User login(String username, String password) throws RemoteException;
    boolean authorize(int userID, UserRole requiredRole) throws RemoteException;
}

// AdminInterface.java
public interface AdminInterface extends Remote {
    List<LogEntry> viewLogs(int adminID) throws RemoteException;
    // Other admin-specific operations
}

// ReportInterface.java
public interface ReportInterface extends Remote {
    Report generateReport(int adminID, ReportType type, String startDate, String endDate) throws RemoteException;
    List<Report> getReportHistory(int adminID) throws RemoteException;
}
```

### RMI Server Flow

```
ServerDriver.main()
    └─> RmiServer.start()
            ├─> Start Apache Derby (embedded mode)
            ├─> DatabaseSetup.initialize()  (create tables + seed admin)
            ├─> LocateRegistry.createRegistry(1099)
            ├─> AuthenticationInterface → Naming.rebind("AuthenticationService")
            ├─> AdminInterface → Naming.rebind("AdminService")
            ├─> ReportInterface → Naming.rebind("ReportService")
            └─> Server ready. Waiting for client connections...
```

### Client Connection Flow (Admin GUI)

```
AdminLoginFrame
    └─> lookup "rmi://localhost:1099/AuthenticationService"
            └─> login() → returns User object if valid
                    └─> AdminDashboardFrame (opens on success)
                            ├─> ReportPanel → lookup "rmi://localhost:1099/ReportService"
                            ├─> ReportResultPanel → display results
                            └─> LogViewerPanel → lookup "rmi://localhost:1099/AdminService"
```

---

## Multi-threading Strategy

| Approach | Where Used | Rationale |
|----------|-----------|-----------|
| **RMI default threading** (thread-per-request) | Most operations (login, view logs, browse history) | Simple, sufficient for light operations |
| **ExecutorService thread pool** | `ReportGenerator.java` | Report queries can be heavy — a bounded thread pool prevents resource exhaustion |
| **Database connection pool** | `DerbyConnection.java` | Single connection for embedded Derby; synchronized access for thread safety |

> **Recommendation:** Use a fixed thread pool of size 5 for report generation tasks. This keeps report queries isolated and prevents long-running aggregation queries from blocking other operations.

---

## Fault Tolerance (Basic)

| Scenario | Handling |
|----------|----------|
| Database connection fails | Wait 1s → retry once → log error → return error to client |
| Remote method throws exception | Catch at remote object → log via AuditLogger → return meaningful error to client |
| RMI registry unavailable | Client retries connection up to 3 times with 2s backoff |
| Server crash | All clients lose connection → reconnection button in GUI → retry lookup |
| Concurrent access to DB | Synchronized blocks in DerbyConnection; use transactions where needed |

---

## Report Generation Logic

### 1. Monthly Appointment Report

**Query:** `SELECT a.AppointmentID, p.FirstName, p.LastName, d.Name, a.DateTime, a.Status FROM Appointments a JOIN Patients p ON a.PatientID = p.PatientID JOIN Doctors d ON a.DoctorID = d.DoctorID WHERE MONTH(a.DateTime) = ? AND YEAR(a.DateTime) = ?`

**Output:** Table with columns: AppointmentID, Patient Name, Doctor, Date/Time, Status → Grouped by status with counts.

### 2. Doctor Consultation Report

**Query:** `SELECT d.DoctorID, d.Name AS DoctorName, COUNT(c.ConsultationID) AS TotalConsultations FROM Doctors d LEFT JOIN Consultations c ON d.DoctorID = c.DoctorID WHERE c.ConsultationDate BETWEEN ? AND ? GROUP BY d.DoctorID, d.Name ORDER BY TotalConsultations DESC`

**Output:** Table with Doctor Name, Total Consultations, and optionally a breakdown by month.

### 3. Patient Visit Summary

**Query:** `SELECT p.PatientID, p.FirstName, p.LastName, COUNT(a.AppointmentID) AS TotalVisits, MIN(a.DateTime) AS FirstVisit, MAX(a.DateTime) AS LastVisit FROM Patients p JOIN Appointments a ON p.PatientID = a.PatientID GROUP BY p.PatientID, p.FirstName, p.LastName`

**Output:** Table with Patient Name, Total Visits, First Visit, Last Visit.

---

## Admin GUI (Swing)

| Component | Description |
|-----------|-------------|
| `AdminLoginFrame` | Username + password fields → login button → calls AuthenticationInterface |
| `AdminDashboardFrame` | JTabbedPane with tabs: Reports, Logs |
| `ReportPanel` | Dropdown for report type + date range picker → Generate button → calls ReportInterface |
| `ReportResultPanel` | JTable showing report data with export option |
| `LogViewerPanel` | JTable showing audit logs with refresh button |

---

## Implementation Order

1. **`Constants.java`** — RMI port (1099), Derby DB URL (`jdbc:derby:BrightCareDB;create=true`)
2. **`User.java` & `UserRole.java`** — Entity + enum
3. **`DerbyConnection.java`** — Singleton connection manager
4. **`DatabaseSetup.java`** — Table creation DDL + seed admin account
5. **`AuthenticationInterface.java` & `AuthenticationImpl.java`** — Login & authorization
6. **`AdminInterface.java` & `AdminImpl.java`** — Admin service
7. **`Admin.java`, `Report.java`, `ReportType.java`** — Entity classes
8. **`ReportInterface.java` & `ReportImpl.java`** — Report generation
9. **`ReportGenerator.java`** — SQL query logic (heaviest class)
10. **`AuditLogger.java`** — Thread-safe logging
11. **`RmiServer.java`** — Registry + bind objects
12. **`ServerDriver.java`** — Main entry point
13. **Swing GUI files** — Login → Dashboard → Reports → Logs
14. **`DateUtils.java` & `SslUtil.java`** — Utilities
15. **`BrigthCare_Medical_Centre.java`** — Overall project entry

---

## Integration Points with Other Members

| Other Member | Shared Resource | Contract |
|-------------|----------------|----------|
| **Member 1 (Receptionist)** | USERS table, AuthenticationInterface | Member 1 users must be registered in USERS table; they call `AuthenticationInterface.login()` to authenticate receptionists |
| **Member 2 (Doctor)** | USERS table, AuthenticationInterface | Doctor accounts in USERS table; call `login()` for authentication |
| **Member 3 (Patient)** | USERS table, AuthenticationInterface | Patient accounts in USERS table; call `login()` for authentication |
| **All Members** | AuditLogger | All members should call `AuditLogger.log(userID, action, details)` for every significant operation |

### Shared Database Access

Member 4 provides `DerbyConnection.java` as a shared utility. All members can:
1. Import `DerbyConnection` to get a DB connection
2. Use the `USERS` table for authentication
3. Call `AuditLogger.log()` to write to the LOGS table

> The Derby database file (`BrightCareDB/`) sits in the project root directory and is accessible to all team members' code.

---

## Key Design Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Database mode | Embedded Derby | No separate DB server setup; simple for assignment |
| RMI port | 1099 (default) | Standard, firewall-friendly |
| Password storage | SHA-256 hash | Basic security for assignment scope |
| GUI framework | Swing | Built into JDK, no extra dependencies |
| Report data format | Tabular (JTable in GUI) | Simple display, can be exported |
| SSL/TLS | Optional enhancement | Can be added via `SslUtil.java` if time permits |

---

## Risks & Mitigation

| Risk | Mitigation |
|------|-----------|
| Derby embedded mode concurrency issues | Synchronized connection access; use transactions |
| Large report queries block other operations | ExecutorService thread pool for report generation |
| RMI interface changes affect other members | Freeze interfaces early; share `.class` files |
| Apache Derby JAR missing | Add `derby.jar` to project classpath during setup |
