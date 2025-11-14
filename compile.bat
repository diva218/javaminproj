@echo off
echo Compiling Java files...
javac -cp "lib\sqlite-jdbc-3.46.1.0.jar;javafx-sdk-17.0.2\lib\*" -d out src\application\*.java src\controller\*.java src\database\*.java src\model\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Copying resources...
copy resources\*.fxml out\ >nul
copy resources\*.css out\ >nul

echo Compilation successful!
pause
