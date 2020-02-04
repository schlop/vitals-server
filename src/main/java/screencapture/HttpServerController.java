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

    private HttpsServer httpsServer;
    private ObjectMapper mapper;
    private VitalSignHandler vitalSignHandler;

    private boolean running;
    private ArrayList<Analyzer> analyzerArrayList;

    public HttpServerController(ArrayList<Analyzer> analyzerArrayList) {
        this.analyzerArrayList = analyzerArrayList;
        try {
            String keystoreFilename = "mycert.keystore";
            char[] storepass = "mypassword".toCharArray();
            char[] keypass = "mypassword".toCharArray();
            FileInputStream fIn = new FileInputStream(keystoreFilename);
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(fIn, storepass);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, keypass);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);

            httpsServer = HttpsServer.create(new InetSocketAddress(Integer.parseInt(Config.getInstance().getProp("port"))), 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Failed to create HTTPS server");
                    }
                }
            });
            mapper = new ObjectMapper();
            vitalSignHandler = new VitalSignHandler();
            httpsServer.createContext("/" + Config.getInstance().getProp("url"), vitalSignHandler);
            for (Analyzer analyzer : analyzerArrayList){
                if (analyzer instanceof ImageAnalyzer){
                    httpsServer.createContext("/" + analyzer.getName(), new ChartHandler(analyzer.getName()));
                }
            }
            httpsServer.setExecutor(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void start() {
        running = true;
        httpsServer.start();
        System.out.println("[HTTPS SERVER] Server started");
    }

    public void stop() {
        running = false;
        httpsServer.stop(0);
    }

    public void publishAnalyzers() {
        ArrayList<String> stringData = new ArrayList<String>();
        for (Analyzer analyzer : analyzerArrayList){
            if (analyzer instanceof TextAnalyzer){
                TextAnalyzer textAnalyzer = (TextAnalyzer) analyzer;
                if (textAnalyzer.isPublish()){
                    stringData.add(textAnalyzer.toString());
                }
            }
            if (analyzer instanceof ColorAnalyzer){
                ColorAnalyzer colorAnalyzer = (ColorAnalyzer) analyzer;
                if (colorAnalyzer.isPublish()){
                    stringData.add(colorAnalyzer.toString());
                }
            }
        }
        try {
            String vitalSignsJSON = mapper.writeValueAsString(stringData);
            vitalSignHandler.setVitalSignsJSON(vitalSignsJSON);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
            byte[] jsonBytes = vitalSignsJSON.getBytes();
            HttpsExchange httpsExchange = (HttpsExchange) he;
            Headers h = httpsExchange.getResponseHeaders();
            h.add("Access-Control-Allow-Origin", "*");
            h.add("Content-Type", "application/json");
            he.sendResponseHeaders(200, jsonBytes.length);
            OutputStream os = he.getResponseBody();
            os.write(jsonBytes);
            os.close();
        }
    }

    public class ChartHandler implements HttpHandler {

        private String path;

        public ChartHandler(String path) {
            this.path = path;
        }

        public void handle(HttpExchange he) throws IOException {
            System.out.println("[HTTPS SERVER] Handled http-get request");
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
