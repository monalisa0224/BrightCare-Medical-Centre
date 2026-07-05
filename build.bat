@echo off
setlocal enabledelayedexpansion
set CLASSES=build\classes
set LIBS=lib\derby.jar
set SRCDIR=src

if not exist "%CLASSES%" mkdir "%CLASSES%"

echo Compiling all source files...
set FILES=
for /r %SRCDIR% %%f in (*.java) do set FILES=!FILES! "%%f"
javac -cp "%LIBS%" -d "%CLASSES%" !FILES!

if %errorlevel% equ 0 (
    echo Build successful.
) else (
    echo Build failed.
    pause
)
