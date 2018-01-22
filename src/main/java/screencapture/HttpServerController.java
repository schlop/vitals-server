package screencapture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by Paul on 29/10/2017.
 */
public class HttpServerController{

    private HttpServer server;
    private GetHandler getHandler;
    private ObjectMapper mapper;

    private boolean running;

    public HttpServerController(MainController mainController){
        try {
            mapper = new ObjectMapper();
            server = HttpServer.create(new InetSocketAddress(Config.HTTP_SERVER_PORT), 0);
            getHandler = new GetHandler(mainController);
            server.createContext("/get", getHandler);
            server.setExecutor(null);
            running = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        running = true;
        server.start();
        System.out.println("[HHTP SERVER] Server started");
    }

    public void stop(){
        running = false;
        server.stop(1);
    }

    public void publishNewVitalSigns(ArrayList<VitalSign> vitalSigns){
        try {
            String vitalSignsJSON = mapper.writeValueAsString(vitalSigns);
            getHandler.setVitalSignsJSON(vitalSignsJSON);
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
    public class GetHandler implements HttpHandler{

        private MainController httpServerInterface;
        private String vitalSignsJSON;

        public GetHandler(MainController httpServerInterface){
            this.httpServerInterface = httpServerInterface;
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
}
