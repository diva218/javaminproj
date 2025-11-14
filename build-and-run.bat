@echo off
echo ========================================
echo Food Storage Management - Build
echo ========================================

REM Detect current directory and JavaFX SDK path
setlocal
set "FX_PATH=%~dp0javafx-sdk-17.0.2\lib"

if not exist "%FX_PATH%" (
    echo [ERROR] JavaFX SDK not found in: %FX_PATH%
    pause
    exit /b
)

echo Using JavaFX from: %FX_PATH%
echo.

echo Compiling all Java source files...
if not exist out mkdir out

for /R src %%f in (*.java) do (
    echo Compiling %%f
    javac --module-path "%FX_PATH%" --add-modules javafx.controls,javafx.fxml -d out "%%f"
)

echo.
echo Running application...
REM Your main class is in src\application\Main.java
java --module-path "%FX_PATH%" --add-modules javafx.controls,javafx.fxml -cp out application.Main

echo.
pause
