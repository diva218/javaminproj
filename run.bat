@echo off
echo Starting Food Storage Management Application...
java --module-path javafx-sdk-17.0.2\lib --add-modules javafx.controls,javafx.fxml -cp "out;lib\*" application.Main
pause
