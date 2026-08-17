@echo off
title BrightCare Receptionist Client - 3-Tier Demo
setlocal

rem ============================================================
rem  BrightCare Medical Centre - 3-Tier Demo
rem  TIER 3 : CLIENT
rem  Run this on the CLIENT device.
rem
rem  >>> EDIT demo.properties IN THE PROJECT ROOT: RMI_SERVER_IP = MACHINE RUNNING start_server_demo.bat <<<
rem
rem  In plain (non-SSL) demo mode the receptionist connects to the same
rem  plain RMI registry (port 1099) as the other roles.
rem ============================================================

if not exist "demo.properties" (
    echo Missing demo.properties in the project root. Cannot determine RMI_SERVER_IP.
    pause
    exit /b 1
)
for /f "tokens=1,2 delims==" %%A in (demo.properties) do set %%A=%%B

set CLASSES=build\classes

if not exist "%CLASSES%" (
    echo Please build the project on the server machine first, then copy
    echo the build\classes folder here. Check start_server_demo.bat.
    pause
    exit /b 1
)

echo Starting Receptionist GUI (connecting to %RMI_SERVER_IP%)...
java -Dbrightcare.rmi.host=%RMI_SERVER_IP% -Dbrightcare.ssl.enabled=false -cp "%CLASSES%" brigthcare_medical_centre.gui.receptionist.ReceptionistLoginFrame
pause
