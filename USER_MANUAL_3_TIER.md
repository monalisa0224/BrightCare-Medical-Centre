# BrightCare Medical Centre - 3-Tier Distributed System User Manual

This guide explains how to run the BrightCare Clinic Management System as a **three-tier distributed system** so that each tier runs on a **separate device** over a local network (LAN):

| Tier | What it runs | Demo script |
|------|--------------|-------------|
| **1 – Database** | Apache Derby Network Server (holds all clinic data) | `start_db_server_demo.bat` |
| **2 – Application / Server** | Java RMI server (business logic, binds remote services) | `start_server_demo.bat` |
| **3 – Client** | Swing GUI clients (Admin, Doctor, Patient, Receptionist) | `start_admin_demo.bat`, `start_doctor_demo.bat`, `start_patient_demo.bat`, `start_receptionist_demo.bat` |

> This manual is for the **3-tier (`*_demo.bat`) mode**. The original single-machine mode (`start_server.bat`, `start_admin.bat`, etc.) is unchanged and still works if you prefer to run everything on one computer.

---

## 1. How the three tiers talk to each other

```
+---------------------+        +------------------------+        +---------------------+
| TIER 3 - CLIENT     |  RMI   | TIER 2 - SERVER        |  JDBC  | TIER 1 - DATABASE    |
| Admin / Doctor       | -----> |  RMI registry :1099    | -----> | Derby Network Server |
| Patient / Reception |        |                        |        | port 1527            |
+---------------------+        +------------------------+        +---------------------+
```

- **Clients (Tier 3)** connect to the **RMI registry on the server device (port 1099)** and call remote services (login, booking, reporting).
- **Server (Tier 2)** talks to **Derby over JDBC (port 1527)** on the database device, reading and writing all records there.
- This way the data, the business logic, and the user interface each live on their own machine.

---

## 2. Prerequisites

Before you start, make sure each device has the following:

1. **JDK 8 or newer** (the project was built with Java 8 APIs and runs on modern JDKs too).
   - Verify with: `java -version` and `javac -version`
2. A copy of the **project folder** `BrigthCare_Medical_Centre` (or at least the parts described below).
3. **Derby 10.17 jars** already placed in `lib\demo\` (see Section 3).
4. **All three devices on the same network** (e.g. same Wi-Fi or LAN), each with a **fixed/known IP address**.

### Which files each device needs

- **Database device** needs:
  - `start_db_server_demo.bat`
  - `lib\demo\db\` → `derby.jar`, `derbynet.jar`, `derbyshared.jar`
- **Server device** needs the full project (to compile):
  - `build.bat`, `src\`, `lib\derby.jar`
  - plus `lib\demo\net\` → `derbyclient.jar`, `derbyshared.jar`
- **Client device(s)** only need the **compiled classes**:
  - the `build\classes` folder (copy it from the server device)

---

## 3. Preparing the Derby jars (one-time)

The full Derby 10.17 distribution provides the network server and client driver needed for a remote database. Copy these jars into the project under `lib\demo\` (this has already been prepared in this project, but repeat if you are setting up a fresh copy):

**On the database device** → copy into `lib\demo\db\`:
- `derby.jar`
- `derbynet.jar`
- `derbyshared.jar`

**On the server device** → copy into `lib\demo\net\`:
- `derbyclient.jar`
- `derbyshared.jar`

---

## 4. Building the project (once, on the server device)

1. Open a terminal in the project root `C:\JavaFolder\BrigthCare_Medical_Centre`.
2. Run:
   ```
   build.bat
   ```
   This compiles all Java sources into `build\classes`. You should see `Build successful.`

> The server itself does this automatically the first time if `build\classes` is missing.

---

## 5. Setting the IP addresses

The demo scripts contain **placeholders** that you must set to your real device IPs. Find your IP on each machine (on Windows, run `ipconfig` and look for **IPv4 Address**).

### 5.1 On the server device — edit `start_server_demo.bat`

```bat
set DB_SERVER_IP=192.168.1.100   <- IP of the DATABASE device
set RMI_SERVER_IP=192.168.1.101  <- IP of THIS server device
```

### 5.2 On each client device — edit the `start_*_demo.bat` files

In every client script, set the server address:

```bat
set RMI_SERVER_IP=192.168.1.101  <- IP of the SERVER device
```

(There is no need to edit `start_db_server_demo.bat` unless you change the Derby port.)

---

## 6. Opening the firewall ports

Each device must **allow inbound traffic** on the relevant port. On Windows, use Windows Defender Firewall → *Advanced settings* → *Inbound Rules* → *New Rule*, or run PowerShell as administrator:

- **Database device** — open TCP **1527**
  ```powershell
  New-NetFirewallRule -DisplayName "BrightCare Derby" -Direction Inbound -Protocol TCP -LocalPort 1527 -Action Allow
  ```
- **Server device** — open TCP **1099**
  ```powershell
  New-NetFirewallRule -DisplayName "BrightCare RMI" -Direction Inbound -Protocol TCP -LocalPort 1099 -Action Allow
  ```

> Tip: make sure all three devices can ping each other (`ping <IP>`) before the demo.

---

## 7. Starting the system (recommended order)

Start the tiers in this order: **Database → Server → Clients**.

### Step 1 – Start the DATABASE tier (on the database device)

```bat
start_db_server_demo.bat
```

Wait until you see a message similar to:

```
Apache Derby Network Server - 10.17.x.x started and ready to accept connections on port 1527
```

Keep this window open. The database is now listening on **port 1527**.

### Step 2 – Start the SERVER tier (on the server device)

```bat
start_server_demo.bat
```

The server will:
1. Connect to the remote Derby database (creating it automatically on first run).
2. Set up all clinic tables and seed the default accounts.
3. Start the RMI registry on **port 1099** and bind the services (Admin, Patient, Doctor, Receptionist, Report, Authentication).

Wait until you see:

```
Database initialized successfully.
BrightCare Medical Centre RMI Server is ready.
```

Keep this window open.

### Step 3 – Start the CLIENT tier (on any client device)

Run the matching client script:

```bat
start_admin_demo.bat      # Administrator
start_doctor_demo.bat     # Doctor
start_patient_demo.bat    # Patient
start_receptionist_demo.bat  # Receptionist
```

A login window opens. Enter the demo account for that role (Section 8).

---

## 8. Demo accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Doctor | `doctor1` | `doctor123` |
| Patient | `patient1` | `patient123` |
| Receptionist | `receptionist1` | `receptionist123` |

Only log in with the account that matches the client you opened (e.g. use `admin` in the Admin client).

---

## 9. What to demonstrate

- **Patient** – view profile, check doctor availability, book/cancel appointments, view history.
- **Doctor** – view pending appointments, accept/reject, reschedule, update consultation notes.
- **Receptionist** – register / search / update / delete patient records.
- **Admin** – user management, view audit logs, and generate the three reports (monthly appointments, doctor consultations, patient visits).

To prove it is a **true 3-tier system**, point out:
- The client GUI updates records that are stored on the **database device**.
- The **server device** has no Swing client open – it only hosts business logic.
- If you switch off the database device after login, operations start failing, showing that data lives on its own tier.

---

## 10. Shutting down

1. Close all client windows (Tier 3).
2. On the server device, press **Enter** in the server window (it shuts down gracefully), then close it.
3. On the database device, press **Ctrl+C** or close the window to stop the Derby Network Server.

---

## 11. Troubleshooting

| Problem | Likely cause / fix |
|---------|--------------------|
| Client says *Cannot connect to server* | `RMI_SERVER_IP` is wrong, the server tier is not running, or port **1099** is blocked by the firewall. |
| Server says it cannot reach the database | `DB_SERVER_IP` is wrong, the DB tier is not running, port **1527** is blocked, or the Derby jars under `lib\demo\db\` / `lib\demo\net\` are missing. |
| Client classes are missing | Copy `build\classes` from the server device to the same relative path on the client device. |
| *Driver not found* on the server | `derbyclient.jar` / `derbyshared.jar` are not in `lib\demo\net\`. |
| Port already in use | Another RMI/Derby instance is running. Close it, or change ports in the scripts (and in `Constants.java` if needed). |
| First connection is slow | Derby creates the database on first run; subsequent runs are faster. |

---

## 12. Quick reference – files

| File | Purpose |
|------|---------|
| `start_db_server_demo.bat` | Tier 1 – starts the Derby database server (port 1527) |
| `start_server_demo.bat` | Tier 2 – starts the RMI server (port 1099) and connects to the remote DB |
| `start_admin_demo.bat` / `start_doctor_demo.bat` / `start_patient_demo.bat` / `start_receptionist_demo.bat` | Tier 3 – start the client GUIs |
| `lib\demo\db\` | Derby network-server jars (database device) |
| `lib\demo\net\` | Derby client-driver jars (server device) |
| `build.bat` | Compiles the project into `build\classes` |
