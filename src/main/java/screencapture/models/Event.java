package screencapture.models;

import j2html.tags.Tag;
import org.json.JSONObject;

import static j2html.TagCreator.*;

import java.util.HashMap;

public class Event {
    private String id;
    private String type;
    private String message;
    private int delay;
    private HashMap<String, String> extras;

    public Event(String id, String type, String message, int delay, HashMap<String, String> extras) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.delay = delay;
        this.extras = extras;
    }

    public String getId() {
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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("message", message);
        json.put("extras", extras);
        return json;
    }

    public Tag toHTML() {
        return div(attrs(".list-group-item"),
                div(attrs(".row .align-items-center"),
                        div(attrs(".col-10"),
                                div(attrs(".row mb-2"),
                                        div(attrs(".col"), message)),
                                div(attrs(".row justify-content-start"),
                                        div(attrs(".col-auto"),
                                                small(attrs(".text-muted"), "Type: " + type)),
                                        div(attrs(".col-auto"),
                                                iff(delay != 0, small(attrs(".text-muted"), "Delay: " + delay))),
                                        div(attrs(".col-auto"),
                                                iff(extras.size() != 0, small(attrs(".text-muted"), "Extras: " + extras.size()))))),
                        div(attrs(".col .ml-auto"),
                                button(attrs(".btn .btn-secondary .float-right .send-button"), "Send").withName(String.valueOf(id)))));
    }
}
