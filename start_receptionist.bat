@echo off
title BrightCare Clinic - Receptionist Module
echo Starting Receptionist Portal...
java -Dbrightcare.receptionist.rmi.port=1100 ^
     -Dbrightcare.ssl.enabled=true ^
     -Djavax.net.ssl.trustStore=clienttrust.jks ^
     -Djavax.net.ssl.trustStorePassword=brightcare123 ^
     -cp "build\classes;lib\*" brigthcare_medical_centre.gui.receptionist.ReceptionistLoginFrame
pause
