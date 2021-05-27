package screencapture.controllers;

import com.sun.net.httpserver.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import screencapture.Config;
import screencapture.models.Event;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static j2html.TagCreator.*;


/**
 * Created by Paul on 29/10/2017.
 * <p>
 * Publishes the vital sign data via HTTPS
 */
public class WebUiController {

    private HttpServer httpServer;
    private ArrayList<Event> eventList;
    private boolean running;

    public WebUiController() {
        eventList = new ArrayList<Event>();
        readEventConfig();
        try {
            httpServer = HttpServer.create(new InetSocketAddress(9555), 0);
            httpServer.createContext("/", new WebUiHandler());
            File[] folder = new File("src/main/static").listFiles();
            for(File file : folder){
                String name = "/" + file.getName();
                httpServer.createContext(name, new FileHandler());
            }
            httpServer.setExecutor(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readEventConfig(){
        try {
            File xml = new File(Config.getInstance().getProp("eventsConfig"));
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();
            NodeList eventNodeList = doc.getElementsByTagName("events").item(0).getChildNodes();
            for (int i = 0; i < eventNodeList.getLength(); i++) {
                String message = "";
                int delay = 0;
                HashMap<String, String> extras = new HashMap<String, String>();
                Node eventNode = eventNodeList.item(i);
                NodeList eventAttributeList = eventNode.getChildNodes();
                for (int j = 0; j < eventAttributeList.getLength(); j++) {
                    Node attribute = eventAttributeList.item(j);
                    switch (attribute.getNodeName()) {
                        case "message":
                            message = attribute.getFirstChild().getNodeValue();
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
                System.out.println(i);
                Event event = new Event(i, message, delay, extras);
                eventList.add(event);
            }
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
                String generated = each(eventList, event ->
                        div(attrs(".list-group-item"),
                                div(attrs(".row .align-items-center"),
                                        div(attrs(".col"), event.getMessage()),
                                        div(attrs(".col-2"),
                                                button(attrs(".btn .btn-primary .float-right"), "Send").withName(String.valueOf(event.getId())))))).render();
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
            System.out.println("[HTTP SERVER] Handled http-get request");
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
}