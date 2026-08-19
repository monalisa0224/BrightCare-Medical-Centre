@echo off
title BrightCare Server - 3-Tier Demo
setlocal

rem ============================================================
rem  BrightCare Medical Centre - 3-Tier Demo
rem  TIER 2 : APPLICATION / RMI SERVER
rem  Run this on the APPLICATION (server) device.
rem
rem  >>> EDIT demo.properties IN THE PROJECT ROOT TO MATCH YOUR NETWORK <<<
rem    DB_SERVER_IP  -> IP of the machine running start_db_server_demo.bat
rem    RMI_SERVER_IP -> IP of THIS machine (must be reachable by clients)
rem ============================================================

if not exist "demo.properties" (
    echo Missing demo.properties in the project root.
    echo Please create it with DB_SERVER_IP and RMI_SERVER_IP.
    pause
    exit /b 1
)
for /f "tokens=1,2 delims==" %%A in (demo.properties) do set %%A=%%B

set CLASSES=build\classes
rem Derby client driver + shared libs used to reach the remote database tier.
set DB_LIBS=lib\demo\net\derbyclient.jar;lib\demo\net\derbyshared.jar

if not exist "%CLASSES%" (
    echo Compiling first...
    call build.bat
    if errorlevel 1 (
        echo Build failed. Cannot start server.
        pause
        exit /b 1
    )
)

echo Starting BrightCare RMI Server (3-tier demo)...
echo RMI host : %RMI_SERVER_IP%
echo Database : jdbc:derby://%DB_SERVER_IP%:1527/BrightCareDB

java -Dbrightcare.rmi.port=1099 ^
     -Dbrightcare.receptionist.rmi.port=1100 ^
     -Dbrightcare.rmi.host=%RMI_SERVER_IP% ^
     -Dbrightcare.db.url=jdbc:derby://%DB_SERVER_IP%:1527/BrightCareDB;create=true ^
     -Dbrightcare.ssl.enabled=true ^
     -Djavax.net.ssl.keyStore=server.jks ^
     -Djavax.net.ssl.keyStorePassword=brightcare123 ^
     -cp "%CLASSES%;%DB_LIBS%" brigthcare_medical_centre.server.ServerDriver
pause
