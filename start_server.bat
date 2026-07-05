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
java -cp "%CLASSES%;%LIBS%" brigthcare_medical_centre.server.ServerDriver
pause
