package screencapture;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Paul on 12/09/2017.
 */

public class MainController {

    private ScheduledExecutorService scheduler;
    private HttpServerController hc;
    private ScreenCaptureController sc;
    private Logger log;

    public static void main(String[] args) {
        MainController mc = new MainController();
        if (Config.getInstance().getProp("httpsEnabled").equals("true")) {
            mc.start();
        } else {
            mc.startWithoutServer();
        }
    }

    public MainController() {
        sc = new ScreenCaptureController(this);
        hc = new HttpServerController(this);
        log = new Logger();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
//        scheduler.scheduleAtFixedRate(sc, 0, Integer.parseInt(Config.getInstance().getProp("captureInterval")), TimeUnit.MILLISECONDS);
        hc.start();
        sc.start();
    }

    public void startWithoutServer() {
        //scheduler.scheduleAtFixedRate(sc, 0, Integer.parseInt(Config.getInstance().getProp("captureInterval")), TimeUnit.MILLISECONDS);
        sc.start();
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
            if (vs.getVitalSignType() == Enums.VITAL_SIGN_TYPE.ALARM_LEVEL) {
                int op = vs.getOp();
                String alarmLevel = vs.getValue();
                for (VitalSign alarmvs : vitalSigns) {
                    if (alarmvs.getVitalSignType() == Enums.VITAL_SIGN_TYPE.ALARM && alarmvs.getOp() == op) {
                        String alarmMessage = alarmvs.getValue();
                        log.logNewAlarm(op, alarmLevel, alarmMessage);
                    }
                }
            }
        }
    }
}




