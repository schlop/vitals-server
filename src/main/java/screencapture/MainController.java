package screencapture;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Paul on 12/09/2017.
 * Starts the application by initializing https server and screen capture controller
 * Capture interval is now managed by the screen capture controller
 */
public class MainController {
    private ScreenCaptureController sc;
    private Communicator communicator;
    private Logger logger;

    public static void main(String[] args) {
        MainController mc = new MainController();
        mc.start();
    }

    public MainController() {
        communicator = new Communicator();
        logger = new Logger();
        sc = new ScreenCaptureController(logger, communicator);
    }

    public void start() {
        sc.start();
    }

    public void stop() {
    }

}




