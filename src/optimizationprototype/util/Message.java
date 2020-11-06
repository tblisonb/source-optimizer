package optimizationprototype.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Message {

    private String contents;
    public final Type type;
    private LocalDateTime time;

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
        SUGGESTION
    }

}
