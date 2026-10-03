@echo off
echo Compiling Hospital Management System...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin src\com\hospital\model\*.java src\com\hospital\singleton\*.java src\com\hospital\factory\*.java src\com\hospital\abstractfactory\*.java src\com\hospital\bridge\*.java src\com\hospital\chain\*.java src\com\hospital\observer\*.java src\com\hospital\proxy\*.java src\com\hospital\*.java
if %errorlevel% equ 0 (
    echo Compilation Successful! Output in bin/
) else (
    echo Compilation Failed!
)
pause
