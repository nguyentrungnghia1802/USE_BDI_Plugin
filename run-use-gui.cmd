@echo off
cd /d "%~dp0"
if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java.exe" -jar "use-gui\target\use-gui.jar" -nr
) else (
    java -jar "use-gui\target\use-gui.jar" -nr
)
