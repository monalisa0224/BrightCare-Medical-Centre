@echo off
title BrightCare Server
set CLASSES=build\classes
set LIBS=lib\derby.jar

if not exist "%CLASSES%" (
    echo Compiling first...
    call build.bat
    if errorlevel 1 (
        echo Build failed. Cannot start server.
        pause
        exit /b 1
    )
)

echo Starting BrightCare RMI Server...
java -Dbrightcare.rmi.port=1099 ^
     -Dbrightcare.receptionist.rmi.port=1100 ^
     -Dbrightcare.ssl.enabled=true ^
     -Djavax.net.ssl.keyStore=server.jks ^
     -Djavax.net.ssl.keyStorePassword=brightcare123 ^
     -cp "%CLASSES%;%LIBS%" brigthcare_medical_centre.server.ServerDriver
pause
