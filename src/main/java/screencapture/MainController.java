package screencapture;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paul on 12/09/2017.
 */

public class MainController implements ScreenAnalyzer.ScreenAnalyzerInterface, HttpServer.HttpServerInterface{

    HttpServer hs;
    ScreenAnalyzer sa;

    public static void main(String[] args) {
        new MainController();
    }

    public MainController(){
        sa = new ScreenAnalyzer(this);
        hs = new HttpServer(this);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(sa, 0, Config.CAPTURE_INTERVAL, TimeUnit.MILLISECONDS);
        scheduler.execute(hs);
    }

    public void vitalSignUpdate(ArrayList<VitalSign> vitalSigns) {
        System.out.println("Received new VS");
        hs.publishNewVitalSigns(vitalSigns);
    }

    public void httpServerUpdate(String put) {

    }
}




