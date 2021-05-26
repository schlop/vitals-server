package screencapture;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by Paul on 29/10/2017.
 *
 * Publishes the vital sign data via HTTPS
 */
public class WebUiController {

    private HttpServer httpServer;
    private WebUiHandler webUiHandler;


    private boolean running;
    private ArrayList<Analyzer> analyzerArrayList;

    public WebUiController() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(9555), 0);
            webUiHandler = new WebUiHandler();
            httpServer.createContext("/", webUiHandler);
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

    public boolean isRunning() {
        return running;
    }

    /*
    - - - - - - - - - - - - G E T - - - - - - - - - - - -
     */
    public class WebUiHandler implements HttpHandler {


        public WebUiHandler() {
        }


        public void handle(HttpExchange he) throws IOException {
            String response = "Hello world";
            System.out.println("[HTTP SERVER] Handled http-get request");
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}