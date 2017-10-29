package screencapture;

import java.util.ArrayList;

/**
 * Created by Paul on 29/10/2017.
 */
public class HttpServer implements Runnable {

    HttpServerInterface httpServerInterface;

    public HttpServer(HttpServerInterface httpServerInterface){
        this.httpServerInterface = httpServerInterface;
    }

    public void run() {
        System.out.println("Http Server Thread was started");
        while (true){
            //do http server stuff here in again (!) a new thread
            //http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
        }
    }

    public void publishNewVitalSigns(ArrayList<VitalSign> vitalSigns){
        System.out.println("HTTP Server received new VS");
        //save current VS
    }

    public interface HttpServerInterface{
        public void httpServerUpdate(String put);
    }
}
