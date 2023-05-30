package optimizationprototype.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private final String contents;
    public final Type type;
    private final LocalDateTime time;

    public Message(String contents, Type messageType) {
        this.contents = contents;
        this.type = messageType;
        this.time = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(time) + " - " + contents;
    }

    public enum Type {
        GENERAL,
        ERROR,
        SUGGESTION,
        COMPILER
    }

}
