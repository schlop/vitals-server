package screencapture.models;

import j2html.tags.Tag;

import static j2html.TagCreator.*;
import static j2html.TagCreator.attrs;

public class LogEntry {
    private String id;
    private String entity;
    private String message;

    public LogEntry(String id, String entity, String message) {
        this.id = id;
        this.entity = entity;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getEntity() {
        return entity;
    }

    public String getMessage() {
        return message;
    }

    public Tag toHTML() {
        return div(attrs(".list-group-item"),
                div(attrs(".row .align-items-center"),
                        div(attrs(".col-10"), message),
                        div(attrs(".col .ml-auto"),
                                button(attrs(".btn .btn-secondary .float-right .send-button"), "Log").withName(String.valueOf(id)))));
    }
}
