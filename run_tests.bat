@echo off
timeout /t 3
java -cp build\classes;build\test\classes;lib\derby.jar;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar org.junit.runner.JUnitCore brigthcare_medical_centre.admin.AdminIntegrationTest
pause