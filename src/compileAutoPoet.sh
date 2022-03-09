#!/bin/sh

DIRM=`pwd` # assumes src/  
echo DIRM: $DIRM

DIRM_L="$DIRM/../lib"
echo DIRM_L: $DIRM_L

JSON_DIR="$DIRM_L/json-20210307.jar"

JUNIT_DIR="$DIRM_L/junit-platform-console-standalone-1.8.1.jar"

CLASSPATH=".:$CLASSPATH:$DIRM:$JSON_DIR:$JUNIT_DIR:"$DIRM"/gui"
echo CLASSPATH: $CLASSPATH

JAVAFX_DIR="$DIRM_L/javafx-sdk-17.0.1/lib/"
echo MODULEPATH: $JAVAFX_DIR

echo "javac --module-source-path module-name="$JAVAFX_DIR"javafx.media -cp "$CLASSPATH" gui/AutoPoet.java -d "$DIRM""

javac --module-path "$JAVAFX_DIR" --add-modules javafx.media,javafx.fxml,javafx.controls -cp "$CLASSPATH" */*.java #-d "$DIRM"
