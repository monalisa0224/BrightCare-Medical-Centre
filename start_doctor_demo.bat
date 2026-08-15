@echo off
title BrightCare Doctor Client - 3-Tier Demo
setlocal

rem ============================================================
rem  BrightCare Medical Centre - 3-Tier Demo
rem  TIER 3 : CLIENT
rem  Run this on the CLIENT device.
rem
rem  >>> CHANGE THIS IP TO THE MACHINE RUNNING start_server_demo.bat <<<
rem ============================================================

set RMI_SERVER_IP=192.168.100.103
set CLASSES=build\classes

if not exist "%CLASSES%" (
    echo Please build the project on the server machine first, then copy
    echo the build\classes folder here. Check start_server_demo.bat.
    pause
    exit /b 1
)

echo Starting Doctor GUI (connecting to %RMI_SERVER_IP%)...
java -Dbrightcare.rmi.host=%RMI_SERVER_IP% -Dbrightcare.ssl.enabled=false -cp "%CLASSES%" brigthcare_medical_centre.gui.doctor.DoctorLoginFrame
pause
