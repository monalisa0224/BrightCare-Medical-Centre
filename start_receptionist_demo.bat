@echo off
title BrightCare Receptionist Client - 3-Tier Demo
setlocal

rem ============================================================
rem  BrightCare Medical Centre - 3-Tier Demo
rem  TIER 3 : CLIENT
rem  Run this on the CLIENT device.
rem
rem  >>> CHANGE THIS IP TO THE MACHINE RUNNING start_server_demo.bat <<<
rem
rem  In plain (non-SSL) demo mode the receptionist connects to the same
rem  plain RMI registry (port 1099) as the other roles.
rem ============================================================

set RMI_SERVER_IP=192.168.1.101
set CLASSES=build\classes

if not exist "%CLASSES%" (
    echo Please build the project on the server machine first, then copy
    echo the build\classes folder here. (start_server_demo.bat builds it.)
    pause
    exit /b 1
)

echo Starting Receptionist GUI (connecting to %RMI_SERVER_IP%)...
java -Dbrightcare.rmi.host=%RMI_SERVER_IP% -Dbrightcare.ssl.enabled=false -cp "%CLASSES%" brigthcare_medical_centre.gui.receptionist.ReceptionistLoginFrame
pause
