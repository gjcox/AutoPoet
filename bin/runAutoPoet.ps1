$DIRM = Get-Location # assumes src/  
Write-Output DIRM: ${DIRM}

$DIRM_L="$DIRM/../lib"
Write-Output DIRM_L: ${DIRM_L}

$JSON_DIR="$DIRM_L/json-20210307.jar"

$JUNIT_DIR="$DIRM_L/junit-platform-console-standalone-1.8.1.jar"

$CLASSPATH=".;$CLASSPATH;$DIRM;$JSON_DIR;$JUNIT_DIR;$DIRM/apis;$DIRM/config;$DIRM/exceptions;$DIRM/gui;$DIRM/testing;$DIRM/utils;$DIRM/words"
Write-Output CLASSPATH: ${CLASSPATH}

$JAVAFX_DIR="$DIRM_L/javafx-sdk-17.0.1/lib/"
Write-Output MODULEPATH: $JAVAFX_DIR

Remove-Item .\*\*.class

javac -encoding UTF-8 --module-path $JAVAFX_DIR --add-modules javafx.media,javafx.fxml,javafx.controls -cp $CLASSPATH gui/Controller.java 
javac -encoding UTF-8 --module-path $JAVAFX_DIR --add-modules javafx.media,javafx.fxml,javafx.controls -cp $CLASSPATH gui/AutoPoet.java 

java --module-path $JAVAFX_DIR --add-modules javafx.media,javafx.fxml,javafx.controls -cp $CLASSPATH gui.AutoPoet