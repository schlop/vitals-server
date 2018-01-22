package screencapture;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paul on 12/09/2017.
 */

public class MainController {

    private ScheduledExecutorService scheduler;
    private HttpServerController hc;
    private ScreenCaptureController sc;
    private Logger log;

    public static void main(String[] args) {
        if (args.length != 0){
            Config.ENABLE_HTTP_SERVER = true;
            Config.USE_SCREENSHOT_AS_INPUT = true;
            Config.SAVE_OCR_IMAGES = false;
        }
        for (String s : args) {
            if (s.equals("-serverEnabled")) {
                Config.ENABLE_HTTP_SERVER = true;
            } else if (s.equals("-testDataEnabled")) {
                Config.USE_SCREENSHOT_AS_INPUT = true;
            } else if (s.equals("-validationEnabled")) {
                Config.SAVE_OCR_IMAGES = true;
            } else if (s.equals("-writeResults")) {
            Config.WRITE_RESULTS = true;
        }

        }

        MainController mc = new MainController();
        if (Config.ENABLE_HTTP_SERVER) {
            mc.start();
        }
        else {
            mc.startWithoutServer();
        }
    }

    public MainController() {
        sc = new ScreenCaptureController(this);
        hc = new HttpServerController(this);
        log = new Logger();
        scheduler = Executors.newSingleThreadScheduledExecutor();

        //This is really dirty
//        JFrame frame = new JFrame("Vital Sign Reader");
//        frame.setContentPane(new StartGUI(this).mainPannel);
//        frame.pack();
//        frame.setVisible(true);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(sc, 0, Config.CAPTURE_INTERVAL, TimeUnit.MILLISECONDS);
        hc.start();
    }

    public void startWithoutServer() {
        scheduler.scheduleAtFixedRate(sc, 0, Config.CAPTURE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdown();
        if (hc.isRunning()) {
            hc.stop();
        }
    }

    public void vitalSignUpdate(ArrayList<VitalSign> vitalSigns) {
        //hand over vital signs to the httpserver
        if (hc.isRunning()) {
            hc.publishNewVitalSigns(vitalSigns);
        }
        //hand over alarms to log
        for (VitalSign vs : vitalSigns) {
            if (vs.getVitalSignType() == Config.VITAL_SIGN_TYPE.ALARM_LEVEL) {
                int op = vs.getOp();
                String alarmLevel = vs.getValue();
                for (VitalSign alarmvs : vitalSigns) {
                    if (alarmvs.getVitalSignType() == Config.VITAL_SIGN_TYPE.ALARM && alarmvs.getOp() == op) {
                        String alarmMessage = alarmvs.getValue();
                        log.logNewAlarm(op, alarmLevel, alarmMessage);
                    }
                }
            }
        }
    }

    public void httpServerUpdate(String put) {

    }
}




