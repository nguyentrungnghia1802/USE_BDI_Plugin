@echo off
cd /d "%~dp0"
if not exist "use-gui\lib\plugins" mkdir "use-gui\lib\plugins"
if exist "use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar" copy /y "use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar" "use-gui\lib\plugins\" >nul
if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java.exe" -jar "use-gui\target\use-gui.jar" -nr
) else (
    java -jar "use-gui\target\use-gui.jar" -nr
)
