package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class LogWriter {

    File log = new File("lib" + File.separator + "logs" + File.separator + "log.log");
    FileWriter writer;
    boolean logging;

    public LogWriter(boolean logging) {
        this.logging = logging;
        if (logging) {
            try {
                writer = new FileWriter(log, false);
            } catch (IOException e) {
                System.err.println(
                        String.format("! LogWriter couldn't find %s - logs will not be recorded", log.toPath()));
                this.logging = false;
            }
        }
    }

    public void writeLog(String string) {
        if (logging) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            try {
                writer.write(String.format("%s\t %s\n", timestamp.toString(), string));
                writer.flush();
            } catch (IOException e) {
                System.err.println(String.format("! LogWriter failed to write %s to %s", string, log.toPath()));
            }
        }
    }

}
