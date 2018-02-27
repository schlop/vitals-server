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

    public HttpServerController(MainController mainController) {
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
            if (Config.getInstance().getProp("auth").equals("false")) {
                httpsServer.createContext("/get", vitalSignHandler);
            }
            httpsServer.createContext("/RMDdplL04YjGKTUaN", vitalSignHandler);
            httpsServer.createContext("/qYQgIHLgW0oO2urcb", new ChartHandler(0));
            httpsServer.createContext("/4pCZzJ3TzDIyvxPsw", new ChartHandler(1));
            httpsServer.createContext("/0JBdsF8kJimXUXJSO", new ChartHandler(2));
            httpsServer.createContext("/7gWjVaSMtZwuav3pX", new ChartHandler(3));
            httpsServer.createContext("/k8tSpNlGAE5XbGtxd", new ChartHandler(4));
            httpsServer.createContext("/C1rU6XuFVEcjCWAwT", new ChartHandler(5));
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

        private int op;

        public ChartHandler(int op) {
            this.op = op;
        }

        public void handle(HttpExchange he) throws IOException {
            System.out.println("[HTTPS SERVER] Handled http-get request");
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "image/png");

            File chartImage = new File(Config.getInstance().getProp("extractedChartPath") + "/" + op + ".png");
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
