package screencapture.controllers;

import com.sun.net.httpserver.*;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import publisher.Publisher;
import screencapture.Config;
import screencapture.Logger;
import screencapture.models.Event;
import screencapture.models.LogEntry;
import screencapture.models.Scenario;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

import static j2html.TagCreator.*;


/**
 * Created by Paul on 29/10/2017.
 * <p>
 * Publishes the vital sign data via HTTPS
 */
public class WebUiController {

    private HttpServer httpServer;
    private boolean running;
    private MainController mc;
    private PostHandler ph;

    public WebUiController(MainController mc) {
        readEventConfig();
        try {
            this.mc = mc;
            ph = new PostHandler();
            httpServer = HttpServer.create(new InetSocketAddress(9555), 0);
            httpServer.createContext("/", new WebUiHandler());
            httpServer.createContext("/command", ph);
            File[] folder = new File("src/main/static").listFiles();
            for (File file : folder) {
                String name = "/" + file.getName();
                httpServer.createContext(name, new FileHandler());
            }
            httpServer.setExecutor(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stripEmptyElements(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().length() == 0) {
                    child.getParentNode().removeChild(child);
                    i--;
                }
            }
            stripEmptyElements(child);
        }
    }

    int scenarioCounter = 0;

    private Scenario readScenarioConfig(Node scenarioNode) {
        scenarioCounter++;
        Scenario scenario = new Scenario(scenarioCounter);

        NodeList scenarioChildrenList = scenarioNode.getChildNodes();
        for (int j = 0; j < scenarioChildrenList.getLength(); j++) {
            Node childNode = scenarioChildrenList.item(j);
            if (childNode.getNodeName().equals("event")) {
                scenario.addEvent(readEventConfig(childNode));
            } else if (childNode.getNodeName().equals("log")) {
                scenario.addLog(readLogConfig(childNode));
            }
        }
        return scenario;
    }

    private Event readEventConfig(Node evenNode) {

        NodeList eventChildrenList = evenNode.getChildNodes();

        String type = "UNKNOWN";
        String message = "";
        int delay = 0;
        HashMap<String, String> extras = new HashMap<String, String>();
        for (int j = 0; j < eventChildrenList.getLength(); j++) {
            Node attribute = eventChildrenList.item(j);
            switch (attribute.getNodeName()) {
                case "message":
                    message = attribute.getFirstChild().getNodeValue();
                    break;
                case "type":
                    type = attribute.getFirstChild().getNodeValue();
                    break;
                case "delay":
                    delay = Integer.parseInt(attribute.getFirstChild().getNodeValue());
                    break;
                case "extras":
                    NodeList extraList = attribute.getChildNodes();
                    for (int k = 0; k < extraList.getLength(); k++) {
                        Node extra = extraList.item(k);
                        String extraName = extra.getNodeName();
                        String extraValue = extra.getFirstChild().getNodeValue();
                        extras.put(extraName, extraValue);
                    }
            }
        }
        Event event = new Event(String.valueOf(evenNode.hashCode()), type, message, delay, extras);
        return event;
    }

    private LogEntry readLogConfig(Node logNode) {
        NodeList eventChildrenList = logNode.getChildNodes();

        String entity = "UNKNOWN";
        String message = "";
        HashMap<String, String> extras = new HashMap<String, String>();
        for (int j = 0; j < eventChildrenList.getLength(); j++) {
            Node attribute = eventChildrenList.item(j);
            switch (attribute.getNodeName()) {
                case "message":
                    message = attribute.getFirstChild().getNodeValue();
                    break;
                case "entity":
                    entity = attribute.getFirstChild().getNodeValue();
                    break;
            }
        }
        return new LogEntry(UUID.randomUUID().toString(), entity, message);
    }

    public ArrayList<Scenario> scenarioArrayList = new ArrayList<>();

    private void readEventConfig() {
        try {
            File xml = new File(Config.getInstance().getProp("eventsConfig"));
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            stripEmptyElements(root);
            NodeList experimentNodeList = root.getChildNodes();
            for (int i = 0; i < experimentNodeList.getLength(); i++) {
                Node scenarioNode = experimentNodeList.item(i);
                scenarioArrayList.add(readScenarioConfig(scenarioNode));
            }
            System.out.println("[CONFIG] Event config read");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        running = true;
        httpServer.start();
        System.out.println("[HTTP SERVER] Server started");
    }

    public void stop() {
        running = false;
        httpServer.stop(0);
        ph.stopTransmission();
    }

    public boolean isRunning() {
        return running;
    }

    /*
    TODO Create three handlers
    1) WebUIHandler: Combines static HTML and merges it with XML stuff
    2) FileHandler: Serves css and js based on their name
    3) PostHandler: Gets called when post buttons are being pressed
     */
    /*
    - - - - - - - - - - - - G E T - - - - - - - - - - - -
     */
    public class WebUiHandler implements HttpHandler {

        private String html;

        public WebUiHandler() {
            html = generateHTML();
        }


        private String generateHTML() {
            try {
                String template = new String(Files.readAllBytes(Paths.get("src/main/static/template.html")));
                String generated = div(attrs("#accordionExample .accordion")).with(
                        each(scenarioArrayList, scenario -> scenario.toHTML())).render();
                MessageFormat form = new MessageFormat(template);
                Object[] insert = {generated};
                return form.format(insert);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "An Error occurred";
        }


        public void handle(HttpExchange he) throws IOException {
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            OutputStream os = he.getResponseBody();
            he.sendResponseHeaders(200, html.length());
            PrintStream ps = new PrintStream(os);
            ps.print(html);
            ps.close();
            os.close();
        }
    }

    public class FileHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            Headers h = he.getResponseHeaders();
            String fileName = he.getRequestURI().toString();
            String contentType = "text/" + fileName.split("\\.")[1];
            h.add("Content-Type", contentType);
            String path = "src/main/static/";
            String content = new String(Files.readAllBytes(Paths.get(path + fileName)));
            OutputStream os = he.getResponseBody();
            he.sendResponseHeaders(200, content.length());
            PrintStream ps = new PrintStream(os);
            ps.print(content);
            ps.close();
            os.close();
        }
    }


    public class PostHandler implements HttpHandler {

        HashMap<String, Event> allEvents = new HashMap<String, Event>();
        HashMap<String, LogEntry> allLogs = new HashMap<>();

        public PostHandler() {
            for (Scenario scenario : scenarioArrayList) {
                scenario.getEvents().forEach(event -> allEvents.put(event.getId(), event));
                scenario.getLogs().forEach(log -> allLogs.put(log.getId(), log));
            }
        }

        @Override
        public void handle(HttpExchange he) throws IOException {

            if (he.getRequestMethod().equalsIgnoreCase("POST")) {
                try {
                    Headers requestHeaders = he.getRequestHeaders();

                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

                    InputStream is = he.getRequestBody();

                    byte[] data = new byte[contentLength];
                    int length = is.read(data);

                    Headers responseHeaders = he.getResponseHeaders();

                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, contentLength);

                    OutputStream os = he.getResponseBody();

                    os.write(data);
                    he.close();
                    JSONObject json = new JSONObject(new String(data));

                    String id = "";
                    if (json.has("id")) {
                        id = json.getString("id");
                    }
                    if (id.equals("start")) {
                        mc.activateTransmission();
                        if (Config.getInstance().getProp("logEnabled").equals("true")) {
                            Logger.getInstance().log("Server", "Simulation started");
                        }
                        return;
                    }
                    if (id.equals("stop")) {
                        stopTransmission();
                        return;
                    }
                    if (allEvents.containsKey(id)) {
                        publishEvent(allEvents.get(id));
                        return;
                    }
                    if (allLogs.containsKey(id)) {
                        Logger.getInstance().log(allLogs.get(id).getEntity(), allLogs.get(id).getMessage());
                        return;
                    }
                    if (json.has("entity") && json.has("message") && Config.getInstance().getProp("logEnabled").equals("true")) {
                        Logger.getInstance().log(json.getString("entity"), json.getString("message"));
                        return;
                    }
                } catch (NumberFormatException | IOException e) {
                    System.out.println(e);
                }
            }
        }

        Timer eventTimer = new Timer();

        private void publishEvent(Event event) {
            eventTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            Publisher.INSTANCE.publish(event.toJSON());
                            System.out.println("[WEB SOCKET] Sent event to HWD: " + event.getMessage());
                        }
                    },
                    event.getDelay() * 1000
            );
        }

        private void publishStopEvent() {
            JSONObject stopEvent = new JSONObject();
            stopEvent.put("type", "STOP");
            stopEvent.put("message", "");
            Publisher.INSTANCE.publish(stopEvent);
        }

        public void stopTransmission() {
            mc.deactivateTransmission();
            publishStopEvent();
            eventTimer.cancel();
            eventTimer = new Timer();
            if (Config.getInstance().getProp("logEnabled").equals("true")) {
                Logger.getInstance().log("Server", "Simulation stopped and HWD reset");
            }
        }
    }
}