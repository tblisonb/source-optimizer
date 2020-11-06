package optimizationprototype.util;

import java.util.Vector;

public class Logger extends SubjectBase {

    private Vector<Message> log;
    private static Logger instance = new Logger();

    private Logger() {
        super();
        log = new Vector<>();
    }

    public static Logger getInstance() {
        return instance;
    }

    public void log(Message message) {
        log.add(message);
        signal();
    }

    public void clear() {
        log.clear();
        signal();
    }

    public Message getLatestMessage() {
        if (log.size() > 0)
            return log.get(log.size() - 1);
        return new Message("", Message.Type.GENERAL);
    }

    public Vector<Message> getAllMessages() {
        return this.log;
    }

}
