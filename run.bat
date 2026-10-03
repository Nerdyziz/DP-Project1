@echo off
title Hospital Management System - Design Patterns Lab
cls
echo ======================================================================
echo    COMPILING AND RUNNING HOSPITAL MANAGEMENT SYSTEM (JAVA)
echo ======================================================================
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin src\com\hospital\model\*.java src\com\hospital\singleton\*.java src\com\hospital\factory\*.java src\com\hospital\abstractfactory\*.java src\com\hospital\bridge\*.java src\com\hospital\chain\*.java src\com\hospital\observer\*.java src\com\hospital\proxy\*.java src\com\hospital\*.java
if %errorlevel% neq 0 (
    echo Compilation failed. Please ensure JDK 17+ is installed.
    pause
    exit /b %errorlevel%
)
java -cp bin com.hospital.Main
pause
