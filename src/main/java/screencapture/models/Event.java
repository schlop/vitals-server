package screencapture.models;

import org.json.JSONObject;

import java.util.HashMap;

public class Event {
    private int id;
    private String type;
    private String message;
    private int delay;
    private HashMap<String, String> extras;

    public Event(int id, String type, String message, int delay, HashMap<String, String> extras) {
        this.id = id;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("message", message);
        json.put("extras", extras);
        return json;
    }
}
