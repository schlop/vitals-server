package screencapture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;

/**
 * Created by Paul on 29/10/2017.
 */
public class HttpServerController {

    private HttpServer server;
    private HttpsServer httpsServer;
    private ObjectMapper mapper;
    private VitalSignHandler vitalSignHandler;

    private boolean running;

    public HttpServerController(MainController mainController) {
        String keystoreFilename = "auth.keystore";
        char[] storepass = "mypassword".toCharArray();
        char[] keypass = "mypassword".toCharArray();
        String alias = "alias";
        FileInputStream fIn = null;
        try {
            fIn = new FileInputStream(keystoreFilename);
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(fIn, storepass);
            // display certificate
            Certificate cert = keystore.getCertificate(alias);
            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, keypass);
            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);
            // create https server
            httpsServer = HttpsServer.create(new InetSocketAddress(9555), 0);
            // create ssl context
            SSLContext sslContext = SSLContext.getInstance("SSL");
            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Failed to create HTTPS server");
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

        mapper = new ObjectMapper();
        vitalSignHandler = new VitalSignHandler();
        httpsServer.createContext("/get", vitalSignHandler);
        httpsServer.createContext("/0", new ChartHandler(0));
        httpsServer.createContext("/1", new ChartHandler(1));
        httpsServer.createContext("/2", new ChartHandler(2));
        httpsServer.createContext("/3", new ChartHandler(3));
        httpsServer.createContext("/4", new ChartHandler(4));
        httpsServer.createContext("/5", new ChartHandler(5));
        httpsServer.setExecutor(null);

//        try {
//            mapper = new ObjectMapper();
//            server = HttpServer.create(new InetSocketAddress(Config.HTTP_SERVER_PORT), 0);
//            vitalSignHandler = new VitalSignHandler();
//            server.createContext("/get", vitalSignHandler);
//            server.createContext("/0", new ChartHandler(0));
//            server.createContext("/1", new ChartHandler(1));
//            server.createContext("/2", new ChartHandler(2));
//            server.createContext("/3", new ChartHandler(3));
//            server.createContext("/4", new ChartHandler(4));
//            server.createContext("/5", new ChartHandler(5));
//            server.setExecutor(null);
//            running = false;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void start() {
        running = true;
        httpsServer.start();
        System.out.println("[HHTP SERVER] Server started");
    }

    public void stop() {
        running = false;
        server.stop(1);
    }

    public void publishNewVitalSigns(ArrayList<VitalSign> vitalSigns) {
        try {
            String vitalSignsJSON = mapper.writeValueAsString(vitalSigns);
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
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "application/json");
            he.sendResponseHeaders(200, jsonBytes.length);
            OutputStream os = he.getResponseBody();
            os.write(jsonBytes);
            os.close();
        }
    }

    public class ChartHandler implements HttpHandler {

        private int op;

        public ChartHandler(int op) {
            this.op = op;
        }

        public void handle(HttpExchange he) throws IOException {
            System.out.println("[HTTP SERVER] Handled http-get request");
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "image/png");

            File chartImage = new File(Config.CHART_PATH + "/" + op + ".png");
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
