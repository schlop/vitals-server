package screencapture;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Paul on 30/10/2017.
 *
 * Writes a log file. The file contains:
 * - timestamp
 * - op
 * - alarm level
 * - alarm message
 * Entries are only added when there was a change (alarm level or alarm status changed)
 */
public class Logger {

    private ArrayList<String> previousVitalSignStrings;
    private FileWriter alarmsFileWriter;

    public Logger() {
        previousVitalSignStrings = new ArrayList<String>();
        for (int i = 0; i < 70; i++) {
            previousVitalSignStrings.add("");
        }

        String pattern = "ddMMMM_hhmm";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat(pattern);
        String dateString = date.format(cal.getTime());
        String alarmsFilename = dateString + "_alarms.csv";
        File csvPathAlarms = new File(Config.getInstance().getProp("csvOutputPath") + "/" + alarmsFilename);

        try {
            alarmsFileWriter = new FileWriter(csvPathAlarms);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String csvHeadings = "time,op,level,message";
        writeToCSV(alarmsFileWriter, csvHeadings);
    }

    public void logNewAlarm(int op, String alarmLevel, String alarmMessage) {
        String alarmLevelOp = op + alarmLevel;
        String previousVitalSign = previousVitalSignStrings.get(op);
        if (!previousVitalSign.equals(alarmLevelOp)){
            previousVitalSignStrings.set(op, alarmLevelOp);
            if (alarmLevel.equals(Enums.ALARM_TYPE.WARNING.toString()) || alarmLevel.equals(Enums.ALARM_TYPE.ALARM.toString())){
                String csvString = System.currentTimeMillis() + "," + op + "," + alarmLevel + "," + alarmMessage;
                writeToCSV(alarmsFileWriter, csvString);
                System.out.println("[LOGGER] Wrote status change");
            }
        }
    }

    private void writeToCSV(FileWriter fw, String str) {
        String add = str + "\n";
        try {
            fw.append(add);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
