package screencapture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.*;
import java.util.ArrayList;

/**
 * Created by Paul on 29/10/2017.
 *
 * Publishes the vital sign data via HTTPS
 */
public class HttpServerController {

    private HttpServer httpServer;
    private ObjectMapper mapper;
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
}
