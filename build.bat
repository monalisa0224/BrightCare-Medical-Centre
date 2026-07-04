@echo off
setlocal enabledelayedexpansion
set CLASSES=build\classes
set LIBS=lib\derby.jar
set SRCDIR=src

if not exist "%CLASSES%" mkdir "%CLASSES%"

echo Compiling all source files...
dir /s /B "%SRCDIR%\*.java" > sources.txt
javac -cp "%LIBS%" -d "%CLASSES%" @sources.txt

if %errorlevel% equ 0 (
    echo Build successful.
) else (
    echo Build failed.
    pause
)
