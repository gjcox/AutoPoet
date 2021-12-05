package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class LogWriter {

    private boolean tempLogging;
    File tempLog = new File("lib" + File.separator + "logs" + File.separator + "log.log");
    FileWriter tempWriter;

    File persistentLog = new File("lib" + File.separator + "logs" + File.separator + "persistent.log");
    FileWriter persistentWriter;

    public LogWriter(boolean tempLogging) {
        this.tempLogging = tempLogging;
        try {
            if (tempLogging) {
                tempWriter = new FileWriter(tempLog, false);
            }
            persistentWriter = new FileWriter(persistentLog, true);
        } catch (IOException e) {
            System.err.println(
                    String.format("! LogWriter couldn't find %s or %s - logs will not be recorded",
                            tempLog.toPath(), persistentLog.toPath()));
            this.tempLogging = false;
        }
    }

    public void writeTempLog(String string) {
        if (tempLogging) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            try {
                tempWriter.write(String.format("%s\t %s\n", timestamp.toString(), string));
                tempWriter.flush();
            } catch (IOException e) {
                System.err.println(String.format("! LogWriter failed to write %s to %s", string, tempLog.toPath()));
            }
        }
    }

    public void writePersistentLog(String string) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            persistentWriter.write(String.format("%s\t %s\n", timestamp.toString(), string));
            persistentWriter.flush();
        } catch (IOException e) {
            System.err.println(String.format("! LogWriter failed to write %s to %s", string, persistentLog.toPath()));
        }

    }

}
