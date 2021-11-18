package screencapture.models;

import j2html.tags.Tag;

import static j2html.TagCreator.*;

import java.util.ArrayList;

public class Scenario {
    private ArrayList<Event> events = new ArrayList<>();
    private ArrayList<LogEntry> logs = new ArrayList<>();
    private int scenarioNumber;


    public void addEvent(Event event) {
        events.add(event);
    }

    public void addLog(LogEntry log) {
        logs.add(log);
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public ArrayList<LogEntry> getLogs() {
        return logs;
    }

    public Scenario(int scenarioNumber) {
        this.scenarioNumber = scenarioNumber;
    }

    public Tag toHTML() {
        return div(div(attrs(".card"),
                        div(attrs("#heading" + scenarioNumber + " .card-header"),
                                h5(attrs(".mb-0"),
                                        button(attrs(".btn .btn-link .btn-block .text-left"), "Scenario " + scenarioNumber + " - Events")
                                                .withType("button")
                                                .withData("toggle", "collapse")
                                                .withData("target", "#collapse" + scenarioNumber)
                                                .attr("aria-expanded", "true")
                                                .attr("aria-controls", "collapse" + scenarioNumber)
                                )),
                        div(attrs("#collapse" + scenarioNumber + " .collapse"),
                                div(attrs(".card-body"),
                                        div(attrs(".list-group")).with(
                                                each(events, event ->
                                                        event.toHTML())
                                        ),
                                        button(attrs(".send-all-button .btn .btn-danger .mt-3 .btn-block"), "Send all")))
                                .attr("aria-labelledby", "heading" + scenarioNumber)),
                div(attrs(".card"),
                        div(attrs("#heading2" + scenarioNumber + " .card-header"),
                                h5(attrs(".mb-0"),
                                        button(attrs(".btn .btn-link .btn-block .text-left"), "Scenario " + scenarioNumber + " - Logs")
                                                .withType("button")
                                                .withData("toggle", "collapse")
                                                .withData("target", "#collapse2" + scenarioNumber)
                                                .attr("aria-expanded", "true")
                                                .attr("aria-controls", "collapse2" + scenarioNumber)
                                )),
                        div(attrs("#collapse2" + scenarioNumber + " .collapse"),
                                div(attrs(".card-body"),
                                        div(attrs(".list-group")).with(
                                                each(logs, log ->
                                                        log.toHTML())
                                        )))));
    }
}
