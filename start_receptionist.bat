@echo off
title BrightCare Clinic - Receptionist Module
echo Starting Receptionist Portal...
java -cp "build\classes;lib\*" brigthcare_medical_centre.gui.receptionist.ReceptionistLoginFrame
pause
```eof

Once you save it, first double-click `start_server.bat` to turn the server on. Then, double-click your new `start_receptionist.bat`. Your login screen should pop right up as a standalone app!