@echo off
title BrightCare - Database Tier (Derby Network Server)
setlocal

rem ============================================================
rem  BrightCare Medical Centre - 3-Tier Demo
rem  TIER 1 : DATABASE
rem  Run this on the DATABASE device (the machine holding the data).
rem
rem  Requires full Derby 10.17 jars in lib\demo\db
rem  (derby.jar, derbynet.jar, derbyshared.jar).
rem
rem  The server listens on ALL interfaces (0.0.0.0) port 1527 so the
rem  application/server tier can connect to it over the LAN.
rem ============================================================

set DB_JARS=lib\demo\db\derby.jar;lib\demo\db\derbynet.jar;lib\demo\db\derbyshared.jar

if not exist "lib\demo\db\derbynet.jar" (
    echo Missing Derby network-server jars under lib\demo\db.
    echo Please copy derby.jar, derbynet.jar and derbyshared.jar there.
    pause
    exit /b 1
)

echo Starting Apache Derby Network Server (database tier) on port 1527...
echo Listening on all interfaces. Leave this window open.

java -cp "%DB_JARS%" org.apache.derby.drda.NetworkServerControl start -h 0.0.0.0 -p 1527

if errorlevel 1 (
    echo.
    echo The database server exited with an error. Check the Derby jars.
)
pause
