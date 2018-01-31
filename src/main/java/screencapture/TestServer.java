package screencapture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Created by Paul on 31/01/2018.
 */
public class TestServer {
    private HttpServer server;
    private ObjectMapper mapper;
    private HttpServerController.VitalSignHandler vitalSignHandler;

    private Handler handler;


    public TestServer(String jsonString) {
        try {
            server = HttpServer.create(new InetSocketAddress(Config.HTTP_SERVER_PORT), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler = new Handler(jsonString);
        server.createContext("/get", handler);
        server.setExecutor(null);
        server.start();
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
            System.out.println("[HTTP SERVER] Handled http-get request");
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
