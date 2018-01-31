package screencapture;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Paul on 30/10/2017.
 */
public class Logger {

    private HashMap<Integer, String> previousVitalSignStrings;
    private FileWriter alarmsFileWriter;

    public Logger() {
        previousVitalSignStrings = new HashMap<Integer, String>();

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
        String csvString = op + "," + alarmLevel + "," + alarmMessage;
        if (!previousVitalSignStrings.containsValue(csvString)) {
            String csvStringTime = System.currentTimeMillis() + "," + csvString;
            writeToCSV(alarmsFileWriter, csvStringTime);
            previousVitalSignStrings.put(op, csvString);
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
