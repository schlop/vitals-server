package screencapture.utils;

import com.sun.net.httpserver.*;
import screencapture.Config;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

/**
 * Created by Paul on 31/01/2018.
 */
public class TestServer {
    private HttpsServer httpsServer;
    private Handler handler;


    public TestServer(String jsonString) {
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
            handler = new Handler(jsonString);
            httpsServer.createContext("/RMDdplL04YjGKTUaN", handler);
            httpsServer.setExecutor(null);
            httpsServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFile(String jsonString) {
        handler.setFile(jsonString);
    }

    /*
    - - - - - - - - - - - - G E T - - - - - - - - - - - -
     */
    public class Handler implements HttpHandler {

        String jsonString;

        public Handler(String jsonString) {
            this.jsonString = jsonString;
        }

        public void setFile(String jsonString){
            this.jsonString = jsonString;
        }

        public void handle(HttpExchange he) throws IOException {
            //System.out.println("[HTTP SERVER] Handled http-get request");
            byte[] jsonBytes = jsonString.getBytes();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "application/json");
            he.sendResponseHeaders(200, jsonBytes.length);
            OutputStream os = he.getResponseBody();
            os.write(jsonBytes);
            os.close();
        }
    }

}
