@echo off
setlocal enabledelayedexpansion
set CLASSES=build\classes
set TEST_CLASSES=build\test\classes
set DERBY_LIB=lib\derby.jar
set JUNIT_LIB=lib\junit-4.13.2.jar
set HAMCREST_LIB=lib\hamcrest-core-1.3.jar
set TESTDIR=test

if not exist "%CLASSES%" (
    echo Compiling application first...
    call build.bat
    if errorlevel 1 exit /b 1
)

if exist "%TEST_CLASSES%" rmdir /s /q "%TEST_CLASSES%"
mkdir "%TEST_CLASSES%"

set HAS_JUNIT=0
if exist "%JUNIT_LIB%" (
    if exist "%HAMCREST_LIB%" (
        set HAS_JUNIT=1
    )
)

if "%HAS_JUNIT%"=="1" (
    set LIBS=%DERBY_LIB%;%JUNIT_LIB%;%HAMCREST_LIB%
    echo JUnit libraries detected. Running full test suite...
    set TEST_FILES=
    for /r %TESTDIR% %%f in (*.java) do set TEST_FILES=!TEST_FILES! "%%f"
    javac -cp "%LIBS%;%CLASSES%" -d "%TEST_CLASSES%" !TEST_FILES!
    if errorlevel 1 exit /b 1

    java -cp "%CLASSES%;%TEST_CLASSES%;%LIBS%" org.junit.runner.JUnitCore ^
        brigthcare_medical_centre.auth.UserTest ^
        brigthcare_medical_centre.admin.AdminTest ^
        brigthcare_medical_centre.admin.AdminIntegrationTest ^
        brigthcare_medical_centre.report.ReportTest ^
        brigthcare_medical_centre.report.ReportIntegrationTest
    if errorlevel 1 exit /b 1
) else (
    set LIBS=%DERBY_LIB%
    echo JUnit libraries not found. Running regression smoke tests only.
    javac -cp "%LIBS%;%CLASSES%" -d "%TEST_CLASSES%" test\brigthcare_medical_centre\tests\RegressionSmokeTests.java
    if errorlevel 1 exit /b 1
)

echo Running regression smoke tests...
java -cp "%CLASSES%;%TEST_CLASSES%;%LIBS%" brigthcare_medical_centre.tests.RegressionSmokeTests
if errorlevel 1 exit /b 1
