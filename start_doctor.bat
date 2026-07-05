@echo off
title BrightCare Doctor Client
set CLASSES=build\classes
set LIBS=lib\derby.jar

if not exist "%CLASSES%" (
    echo Please run start_server.bat first to build the project.
    pause
    exit /b 1
)

echo Starting Doctor GUI...
java -cp "%CLASSES%;%LIBS%" brigthcare_medical_centre.gui.doctor.DoctorLoginFrame


