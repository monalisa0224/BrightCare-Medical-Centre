@echo off
setlocal enabledelayedexpansion
set CLASSES=build\classes
set TEST_CLASSES=build\test\classes
set LIBS=lib\derby.jar;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar
set TESTDIR=test

if not exist "%CLASSES%" (
    echo Compiling application first...
    call build.bat
    if errorlevel 1 exit /b 1
)

if exist "%TEST_CLASSES%" rmdir /s /q "%TEST_CLASSES%"
mkdir "%TEST_CLASSES%"

echo Compiling regression smoke tests...
set TEST_FILES=
for /r %TESTDIR% %%f in (*.java) do set TEST_FILES=!TEST_FILES! "%%f"
javac -cp "%LIBS%;%CLASSES%" -d "%TEST_CLASSES%" !TEST_FILES!
if errorlevel 1 exit /b 1

echo Running JUnit test suites...
java -cp "%CLASSES%;%TEST_CLASSES%;%LIBS%" org.junit.runner.JUnitCore ^
    brigthcare_medical_centre.auth.UserTest ^
    brigthcare_medical_centre.admin.AdminTest ^
    brigthcare_medical_centre.admin.AdminIntegrationTest ^
    brigthcare_medical_centre.report.ReportTest ^
    brigthcare_medical_centre.report.ReportIntegrationTest
if errorlevel 1 exit /b 1

echo Running regression smoke tests...
java -cp "%CLASSES%;%TEST_CLASSES%;%LIBS%" brigthcare_medical_centre.tests.RegressionSmokeTests
if errorlevel 1 exit /b 1
