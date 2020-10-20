package optimizationprototype.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class Logger extends SubjectBase {

    private Vector<String> log;
    private DateTimeFormatter time;
    private static Logger instance = new Logger();

    private Logger() {
        super();
        log = new Vector<>();
        time = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    public static Logger getInstance() {
        return instance;
    }

    public void log(String message) {
        log.add(time.format(LocalDateTime.now()) + " - " + message);
        signal();
    }

    public void clear() {
        log.clear();
        signal();
    }

    public String getLatest() {
        if (log.size() > 0)
            return log.get(log.size() - 1);
        return "";
    }

}
