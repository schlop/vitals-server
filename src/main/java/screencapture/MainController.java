package screencapture;

import org.json.JSONObject;
import publisher.Publisher;
import publisher.Subscriber;
import publisher.SubscriberChangeListener;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Paul on 12/09/2017.
 * Starts the application by initializing https server and screen capture controller
 * Capture interval is now managed by the screen capture controller
 */
public class MainController {
    private ScreenCaptureController sc;
    private WebUiController ui;

    public static void main(String[] args) {
        MainController mc = new MainController();
        mc.start();
    }

    public MainController() {
        sc = new ScreenCaptureController();
        ui = new WebUiController();
        if(Config.getInstance().getProp("logEnabled").equals("true")){
            Logger.getInstance();
        }
        Publisher.INSTANCE.startNetworking(8888, 8, true);
    }


    public void start() {
        sc.start();
        ui.start();
    }

    public void stop() {
    }
}




