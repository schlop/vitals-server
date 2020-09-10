package screencapture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.lwawt.macosx.CPrinterDevice;

import javax.net.ssl.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Paul on 29/10/2017.
 *
 * Publishes the vital sign data via HTTPS
 */
public class HttpServerController {

    private HttpServer httpServer;
    private VitalSignHandler vitalSignHandler;


    private boolean running;
    private ArrayList<Analyzer> analyzerArrayList;

    public HttpServerController(ArrayList<Analyzer> analyzerArrayList) {
        this.analyzerArrayList = analyzerArrayList;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(9555), 0);
            vitalSignHandler = new VitalSignHandler();
            httpServer.createContext("/" + Config.getInstance().getProp("url"), vitalSignHandler);
            for (Analyzer analyzer : analyzerArrayList){
                if (analyzer instanceof ImageAnalyzer){
                    httpServer.createContext("/" + analyzer.getName(), new ChartHandler(analyzer.getName()));
                }
            }
            if (!Config.getInstance().getProp("urlExtra").equals("false")){
                httpServer.createContext("/" + Config.getInstance().getProp("urlExtra"), new StaticDataHandler(Config.getInstance().getProp("extraConfig")));
            }
            httpServer.setExecutor(null);
        } catch (Exception e) {
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

    public void publishAnalyzers() {
        String vitalSignsJSON = "";
        for (Analyzer analyzer : analyzerArrayList){
            if (analyzer instanceof TextAnalyzer){
                TextAnalyzer textAnalyzer = (TextAnalyzer) analyzer;
                if (textAnalyzer.isPublish()){
                    vitalSignsJSON += (textAnalyzer.toString() + ',');
                }
            }
            if (analyzer instanceof ColorAnalyzer){
                ColorAnalyzer colorAnalyzer = (ColorAnalyzer) analyzer;
                if (colorAnalyzer.isPublish()){
                    vitalSignsJSON += (colorAnalyzer.toString() + ',');
                }
            }
        }
        vitalSignsJSON = '[' + vitalSignsJSON.substring(0, vitalSignsJSON.length() - 1) + ']';
        vitalSignHandler.setVitalSignsJSON(vitalSignsJSON);
    }

    public boolean isRunning() {
        return running;
    }

    /*
    - - - - - - - - - - - - G E T - - - - - - - - - - - -
     */
    public class VitalSignHandler implements HttpHandler {

        private String vitalSignsJSON;

        public VitalSignHandler() {
            vitalSignsJSON = "No VS set yet!";
        }

        public void setVitalSignsJSON(String vitalSignsJSON) {
            this.vitalSignsJSON = vitalSignsJSON;
        }

        public void handle(HttpExchange he) throws IOException {
            System.out.println("[HTTP SERVER] Handled http-get request");
            he.sendResponseHeaders(200, vitalSignsJSON.length());
            OutputStream os = he.getResponseBody();
            os.write(vitalSignsJSON.getBytes());
            os.close();
        }
    }

    public class ChartHandler implements HttpHandler {

        private String path;
        public ChartHandler(String path) {
            this.path = path;
        }

        public void handle(HttpExchange he) throws IOException {
            System.out.println("[HTTP SERVER] Handled http-get request");
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "image/png");

            File chartImage = new File(Config.getInstance().getProp("extractedChartPath") + "/" + path + ".png");
            byte[] bytes = new byte[(int) chartImage.length()];

            FileInputStream fileInputStream = new FileInputStream(chartImage);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(bytes, 0, bytes.length);

            he.sendResponseHeaders(200, chartImage.length());
            OutputStream outputStream = he.getResponseBody();
            outputStream.write(bytes, 0, bytes.length);
            outputStream.close();
        }
    }

    public class StaticDataHandler implements HttpHandler{
        private long startTime;
        private TreeMap<Integer, String> jsonDataElements;

        public  StaticDataHandler(String path){
            startTime = System.currentTimeMillis();
            jsonDataElements = new TreeMap<Integer, String>();

            try {
                File xml = new File(path);
                DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = dBuilder.parse(xml);
                doc.getDocumentElement().normalize();

                NodeList jsonElement = doc.getElementsByTagName("definition").item(0).getChildNodes();
                for (int i = 0; i < jsonElement.getLength(); i++) {
                    Node analyserNode = jsonElement.item(i);
                    NodeList jsonAttributeList = analyserNode.getChildNodes();
                    Integer time = Integer.parseInt(jsonAttributeList.item(0).getFirstChild().getNodeValue());
                    String content = jsonAttributeList.item(1).getFirstChild().getNodeValue();
                    jsonDataElements.put(time, content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Serve the extra that is currently active, delete the old extra from the map when a new one is active
         * @param he
         * @throws IOException
         */
        public void handle(HttpExchange he) throws IOException {
            for (Map.Entry<Integer, String> entry : jsonDataElements.entrySet()) {
                if (System.currentTimeMillis() > startTime + entry.getKey()){
                    jsonDataElements.remove(entry.getKey());
                }
                else {
                    System.out.println("[HTTP SERVER] Handled http-get request");
                    he.sendResponseHeaders(200, entry.getValue().length());
                    OutputStream os = he.getResponseBody();
                    os.write(entry.getValue().getBytes());
                    os.close();
                    break;
                }
            }
        }
    }
}
