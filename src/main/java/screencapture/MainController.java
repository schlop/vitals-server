package screencapture;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Paul on 12/09/2017.
 * Starts the application by initializing https server and screen capture controller
 * Capture interval is now managed by the screen capture controller
 */
public class MainController {

    private HttpServerController hc;
    private ScreenCaptureController sc;
    private Logger log;

    public static void main(String[] args) {
        MainController mc = new MainController();
        mc.start();
    }

    public MainController() {
        sc = new ScreenCaptureController(this);
        ArrayList<Analyzer> ana = sc.getAnalyzerList();
        hc = new HttpServerController(ana);
        log = new Logger(ana);
    }

    public void start() {
        hc.start();
        sc.start();
    }

    public void stop() {
        if (hc.isRunning()) {
            hc.stop();
        }
    }

    public void vitalSignUpdate() {
        hc.publishAnalyzers();
        if (Boolean.parseBoolean(Config.getInstance().getProp("logEnabled"))){
            log.logAnalyzers();
        }
    }
}




