package screencapture.controllers;

import com.sun.net.httpserver.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import publisher.Publisher;
import screencapture.Config;
import screencapture.models.Event;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
            httpServer.createContext("/command", new PostHandler());
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

    public static void stripEmptyElements(Node node)
    {
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().length() == 0) {
                    child.getParentNode().removeChild(child);
                    i--;
                }
            }
            stripEmptyElements(child);
        }
    }

    private void readEventConfig() {
        try {
            File xml = new File(Config.getInstance().getProp("eventsConfig"));
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            stripEmptyElements(root);
            NodeList eventNodeList = root.getChildNodes();

            for (int i = 0; i < eventNodeList.getLength(); i++) {
                String type = "UNKNOWN";
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
                System.out.println(i);
                Event event = new Event(i, type, message, delay, extras);
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
                                        div(attrs(".col-10"),
                                                div(attrs(".row mb-2"),
                                                        div(attrs(".col"), event.getMessage())),
                                                div(attrs(".row justify-content-start"),
                                                        div(attrs(".col-auto"),
                                                                small(attrs(".text-muted"), "Type: " + event.getType())),
                                                        div(attrs(".col-auto"),
                                                                iff(event.getDelay() != 0, small(attrs(".text-muted"), "Delay: " + event.getDelay()))),
                                                        div(attrs(".col-auto"),
                                                                iff(event.getExtras().size() != 0, small(attrs(".text-muted"), "Extras: " + event.getExtras().size()))))),
                                        div(attrs(".col .ml-auto"),
                                                button(attrs(".btn .btn-secondary .float-right .send-button"), "Send").withName(String.valueOf(event.getId())))))).render();
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

    public class PostHandler implements HttpHandler {

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
                    Event event = eventList.get(Integer.parseInt(new String(data).replaceAll("\\D+","")));
                    publishEvent(event);
                } catch (NumberFormatException | IOException e) {
                }
            }
        }

        private void publishEvent(Event event){
            if(event.getDelay() == 0){
                Publisher.INSTANCE.publish(event.toJSON());
                System.out.println(event.toJSON());
            }
            else{
                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                Publisher.INSTANCE.publish(event.toJSON());
                                System.out.println(event.toJSON());
                            }
                        },
                        event.getDelay() * 1000
                );
            }
        }
    }
}