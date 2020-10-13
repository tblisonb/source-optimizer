package optimizationprototype.util;

import java.util.Vector;

public class Logger extends SubjectBase {

    private Vector<String> log;
    private static Logger instance;

    private Logger() {
        super();
        log = new Vector<>();
    }

    public static Logger getInstance() {
        if (instance == null)
            return new Logger();
        return instance;
    }

    public void log(String message) {
        log.add(message);
        signal();
    }

    public void clear() {
        log.clear();
        signal();
    }

    public String getLatest() {
        return log.get(log.size() - 1);
    }

}
