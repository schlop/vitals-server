package screencapture;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Paul on 30/10/2017.
 */
public class Logger {

    private String[] previousVitalSignStrings;
    private FileWriter alarmsFileWriter;
    private FileWriter reportsFileWriter;

    public Logger(){
        previousVitalSignStrings = new String[Config.NUMBER_OP_THEATERS];

        String pattern = "ddMMMM_hhmm";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat(pattern);
        String dateString = date.format(cal.getTime());

        String alarmsFilename = dateString + "_alarms.csv";
        String reportsFilename = dateString + "_reports.csv";


        File csvPathAlarms = new File(Config.CSV_OUTPUT_PATH + "/" + alarmsFilename);
        File csvPathReports = new File(Config.CSV_OUTPUT_PATH + "/" + reportsFilename);

        try {
            alarmsFileWriter = new FileWriter(csvPathAlarms);
            reportsFileWriter = new FileWriter(csvPathReports);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String csvHeadings = "time,op,level,message";
        writeToCSV(alarmsFileWriter, csvHeadings);
    }

    public void logNewAlarm(int op, String alarmLevel, String alarmMessage){
        String csvString = op + "," + alarmLevel + "," + alarmMessage;
        if (!csvString.equals(previousVitalSignStrings[op])){
            if (!alarmLevel.equals(Config.ALARM_TYPE.NONE.toString())){
                String csvStringTime = System.currentTimeMillis() + "," + csvString;
                writeToCSV(alarmsFileWriter, csvStringTime);
            }
            previousVitalSignStrings[op] = csvString;
        }
    }

    private void writeToCSV(FileWriter fw, String str){
        String add = str + "\n";
        try {
            fw.append(add);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
