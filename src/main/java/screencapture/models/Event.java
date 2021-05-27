package screencapture.models;

import java.util.HashMap;

public class Event {
    private int id;
    private String message;
    private int delay;
    private HashMap<String, String> extras;

    public Event(int id, String message, int delay, HashMap<String, String> extras) {
        this.id = id;
        this.message = message;
        this.delay = delay;
        this.extras = extras;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public int getDelay() {
        return delay;
    }

    public HashMap<String, String> getExtras() {
        return extras;
    }
}
