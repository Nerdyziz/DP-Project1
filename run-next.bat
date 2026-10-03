@echo off
title Mercy General Hospital - Next.js Full Stack Launcher
cls
echo ======================================================================
echo    STARTING MERCY GENERAL HOSPITAL FULL-STACK SYSTEM (JAVA + NEXT.JS)
echo ======================================================================

:: 1. Compile Java Backend
echo [1/3] Compiling Java Backend...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin src\com\hospital\model\*.java src\com\hospital\singleton\*.java src\com\hospital\factory\*.java src\com\hospital\abstractfactory\*.java src\com\hospital\bridge\*.java src\com\hospital\chain\*.java src\com\hospital\observer\*.java src\com\hospital\proxy\*.java src\com\hospital\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Java compilation failed. Please ensure JDK 17+ is installed.
    pause
    exit /b %errorlevel%
)

:: 2. Start Dedicated Java Backend REST API Server in persistent window
echo [2/3] Starting Dedicated Java REST Server (Port 8080)...
start "Mercy Hospital - Java REST Backend (:8080)" java -cp bin com.hospital.WebServer 8080

:: 3. Launch Next.js Frontend
echo [3/3] Starting Next.js Clinical Web Portal (Port 3000)...
cd frontend
start http://localhost:3000/
npm run dev
pause
