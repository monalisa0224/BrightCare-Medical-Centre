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
rem  The receptionist connects to the dedicated TLS RMI registry (port 1100).
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

echo Starting Receptionist GUI with TLS (connecting to %RMI_SERVER_IP%:1100)...
java -Dbrightcare.rmi.host=%RMI_SERVER_IP% ^
     -Dbrightcare.receptionist.rmi.port=1100 ^
     -Dbrightcare.ssl.enabled=true ^
     -Djavax.net.ssl.trustStore=clienttrust.jks ^
     -Djavax.net.ssl.trustStorePassword=brightcare123 ^
     -cp "%CLASSES%" brigthcare_medical_centre.gui.receptionist.ReceptionistLoginFrame
pause
