package screencapture.controllers;

import networking.NetworkingChangeListener;
import networking.NetworkingState;
import org.json.JSONObject;
import publisher.Publisher;
import publisher.Subscriber;
import publisher.SubscriberChangeListener;
import screencapture.Config;
import screencapture.Logger;

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
        ui = new WebUiController(this);
        if(Config.getInstance().getProp("logEnabled").equals("true")){
            Logger.getInstance();
        }
        Publisher.INSTANCE.startNetworking(8888, 8, true);
        Publisher.INSTANCE.addSubscribeListener(new SubscriberChangeListener() {
            @Override
            public void onSubscribe(Subscriber subscriber) {
                System.out.println("[WEB SOCKET] HWD connected");
            }

            @Override
            public void onUnsubscribe(Subscriber subscriber) {
                System.out.println("[WEB SOCKET] HWD disconnected");
            }

            @Override
            public void onChange(Subscriber subscriber) {
                System.out.println("[WEB SOCKET] HWD ready for transmission");
            }

            @Override
            public void onMessage(Subscriber subscriber, JSONObject jsonObject) {

            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                ui.stop();
                System.out.println("[System] Restarting HWD before shutdown");
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void start() {
        sc.start();
        ui.start();
    }

    public void activateTransmission(){
        sc.activateTransmission();
    }

    public void deactivateTransmission(){
        sc.deactivateTransmission();
    }

    public void stop() {
    }
}




