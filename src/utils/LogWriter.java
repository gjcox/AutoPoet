package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Timestamp;

/**
 * Class for log writers.
 * 
 * Does not work correctly within IDEs due to relative file paths.
 * 
 * @author 190021081
 */
public class LogWriter {

    private boolean tempLogging;
    private File tempLog = new File(
            ".." + File.separator + "logs" + File.separator + "log.log");
    private FileWriter tempWriter;

    private File persistentLog = new File(
            ".." + File.separator + "logs" + File.separator + "persistent.log");
    private FileWriter persistentWriter;

    public LogWriter(boolean tempLogging) {

        this.tempLogging = tempLogging;
        try {
            if (tempLogging) {
                tempWriter = new FileWriter(tempLog, StandardCharsets.UTF_8, false);
            }
            persistentWriter = new FileWriter(persistentLog, StandardCharsets.UTF_8, true);
        } catch (IOException e) {
            System.err.println(
                    String.format("! LogWriter couldn't find %s or %s - logs will not be recorded",
                            tempLog.toPath(), persistentLog.toPath()));
            System.err.println(e);
            this.tempLogging = false;
        }
    }

    /**
     * Writes string to per-process log file.
     * 
     * @param string line to write.
     */
    public void writeTempLog(String string) {
        if (tempLogging) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            try {
                tempWriter.write(String.format("%s\t %s\n", timestamp.toString(), string));
            } catch (IOException e) {
                System.err.println(String.format("! LogWriter failed to write %s to %s", string, tempLog.toPath()));
            }
        }
    }

    /**
     * Writes string to persistent log file.
     * 
     * @param string line to write.
     */
    public void writePersistentLog(String string) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            persistentWriter.write(String.format("%s\t %s\n", timestamp.toString(), string));
        } catch (NullPointerException | IOException e) {
            System.err.println(String.format("! LogWriter failed to write %s to %s", string, persistentLog.toPath()));
        }

    }

    /**
     * Closes log writers.
     */
    public void closeLogWriters() {
        try {
            persistentWriter.close();
            if (tempLogging) {
                tempWriter.close();
            }
        } catch (NullPointerException | IOException e) {
            System.err.println("! LogWriter failed to close FileWriters: " + e.getMessage());
        }
    }

    /**
     * For debugging.
     * 
     * @param args should be empty.
     */
    public static void main(String[] args) {
        System.out.println("log.log: " + Paths.get("docs", "logs", "log.log").toFile().exists());
        System.out.println("persistent.log: " + Paths.get("docs", "logs", "persistent.log").toFile().exists());
    }

}
