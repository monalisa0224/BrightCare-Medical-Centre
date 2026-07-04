# BrightCare Medical Centre — Clinic Management System

A distributed clinic management system built with **Java RMI**, **Apache Derby** (embedded database), and **Java Swing GUI**. Developed as a group assignment for Distributed Computer Systems.

## Quick Start

### Prerequisites
- Java 8+ (JDK)
- No database installation needed — Derby runs embedded

### Run the project

**Step 1 — Compile all source files:**
```
Double-click `build.bat`
```
or from terminal:
```
build.bat
```

**Step 2 — Start the RMI Server (keep this window open):**
```
Double-click `start_server.bat`
```
or from terminal:
```
start_server.bat
```

**Step 3 — Launch a client (in a separate terminal):**

| Client | Command | Login |
|--------|---------|-------|
| Admin | `start_admin.bat` | `admin` / `admin123` |
| Patient | `start_patient.bat` | `patient1` / `patient123` |
| Doctor | *(coming soon)* | `doctor1` / `doctor123` |

### Test Accounts (auto-seeded)

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Patient | `patient1` | `patient123` |
| Doctor | `doctor1` | `doctor123` |

## Project Structure

```
src/brigthcare_medical_centre/
├── server/         RMI server startup
├── common/         Remote interfaces (shared contract)
├── auth/           Login & authentication
├── admin/          Admin operations
├── report/         Report generation (3 types)
├── patient/        Patient module
├── database/       Derby connection, setup, audit logging
├── gui/
│   ├── admin/      Admin Swing GUI
│   └── patient/    Patient Swing GUI
└── util/           Constants, date helpers, SSL stubs
```

## Technology Stack

- **Java RMI** — Remote Method Invocation for client-server communication
- **Apache Derby** — Embedded SQL database (zero-config)
- **Java Swing** — Desktop GUI
- **Serialization** — RMI parameter passing across network

## Team Members & Responsibilities

| Member | Module | Key Files |
|--------|--------|-----------|
| Member 1 | Receptionist + Security | Patient registration, SSL, role-based access |
| Member 2 | Doctor | Consultation notes, appointment lists, medical history |
| Member 3 | Patient | Book/cancel appointments, check availability, view history |
| Member 4 | Admin + Server | RMI server, report generation, auth, database setup |

## Database Tables

| Table | Owned By | Purpose |
|-------|----------|---------|
| USERS | Member 4 | Central authentication for all roles |
| LOGS | Member 4 | Audit trail |
| REPORTS | Member 4 | Stored generated reports |
| PATIENTS | Member 1 | Patient registration details |
| DOCTORS | Member 1 | Doctor profiles |
| DOCTOR_SCHEDULE | Member 3 | Available time slots |
| APPOINTMENTS | Member 3 | Appointment bookings |

## Project SOP for Coding

To keep the project organized and easy to maintain, please follow these standard operating procedures when working on the codebase:

1. **Understand the task first**
   - Read the issue, requirement, or assignment instruction carefully.
   - Identify the module or feature you need to change before editing code.

2. **Check the existing code structure**
   - Review related files and follow the current project style.
   - Reuse existing classes, methods, and naming conventions where possible.

3. **Work on a separate branch**
   - Do not code directly on the main branch.
   - Create a new branch for each feature, fix, or improvement.

4. **Make small and clear changes**
   - Keep commits focused on one task at a time.
   - Avoid mixing unrelated changes in the same commit.

5. **Test before pushing**
   - Run the project and verify that your changes work as expected.
   - Fix errors and warnings before creating a pull request.

6. **Write meaningful code comments only when needed**
   - Comment complex logic, but avoid obvious comments.
   - Keep code readable and self-explanatory.

7. **Review your work before submitting**
   - Check formatting, imports, and naming.
   - Make sure there are no debug prints or temporary code left behind.

8. **Coordinate with your group members**
   - Inform the team if you are editing shared files.
   - Avoid overwriting someone else’s work.

## Git Command Guidelines for New GitHub Users

Here are some basic Git commands that can help new contributors work safely and confidently:

### 1. Clone the repository
```bash
git clone <repository-url>
```
Downloads the project to your local machine.

### 2. Check your current branch
```bash
git branch
```
Shows which branch you are currently on.

### 3. Create a new branch
```bash
git checkout -b feature/your-branch-name
```
Creates and switches to a new branch for your work.

### 4. Check file status
```bash
git status
```
Shows which files are changed, staged, or untracked.

### 5. Add changes to staging
```bash
git add .
```
Stages all modified files. You can also add a single file:
```bash
git add README.md
```

### 6. Commit your changes
```bash
git commit -m "Describe your change clearly"
```
Saves your changes locally with a useful message.

### 7. Pull the latest changes
```bash
git pull origin main
```
Updates your local branch with the latest remote changes.

### 8. Push your branch
```bash
git push origin feature/your-branch-name
```
Uploads your branch to GitHub.

### 9. Create a pull request
- Open GitHub in your browser.
- Compare your branch with `main`.
- Add a clear title and description for your changes.

### 10. Useful tips
- Commit frequently with clear messages.
- Pull updates before starting new work.
- Ask for help if you are unsure about merge conflicts.

## Suggested Git Workflow

```bash
git checkout -b feature/new-task
git status
git add .
git commit -m "Add new task implementation"
git pull origin main
git push origin feature/new-task
```

This workflow helps keep the project clean and reduces merge conflicts.
