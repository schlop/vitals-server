package screencapture;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Paul on 30/10/2017.
 * <p>
 * Writes a log file. The file contains:
 * - timestamp
 * - op
 * - alarm level
 * - alarm message
 * Entries are only added when there was a change (alarm level or alarm status changed)
 */
public class Logger {

    //TODO: We either have to create a new thread in here are call from a new thread. Otherwise we block the image recognition with our IO
    private FileWriter alarmsFileWriter;

    public Logger() {

        String pattern = "ddMMMM_hhmm";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat(pattern);
        String dateString = date.format(cal.getTime());
        String alarmsFilename = dateString + "_log.csv";
        File csvPathAlarms = new File(Config.getInstance().getProp("csvOutputPath") + "/" + alarmsFilename);

        try {
            alarmsFileWriter = new FileWriter(csvPathAlarms);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String csvHeadings = "time,analyzer,value";
        writeToCSV(alarmsFileWriter, csvHeadings);
    }

    public void log(String name, String value){
        String csvString = System.currentTimeMillis() + "," + name + "," + value;
        writeToCSV(alarmsFileWriter, csvString);
        System.out.println("[LOGGER] Wrote status change");
    }

//    public void logAnalyzers() {
//        for (Analyzer analyzer : analyzerArrayList) {
//            if (analyzer instanceof TextAnalyzer) {
//                TextAnalyzer textAnalyzer = (TextAnalyzer) analyzer;
//                if (textAnalyzer.isLog() && !textAnalyzer.getValue().equals(textAnalyzer.getPreviousValue())) {
//                    String csvString = System.currentTimeMillis() + "," +
//                            textAnalyzer.getName() + "," +
//                            textAnalyzer.getPreviousValue() +
//                            "," + textAnalyzer.getValue();
//                    writeToCSV(alarmsFileWriter, csvString);
//                    System.out.println("[LOGGER] Wrote status change");
//                }
//            }
//            if (analyzer instanceof ColorAnalyzer) {
//                ColorAnalyzer colorAnalyzer = (ColorAnalyzer) analyzer;
//                if (colorAnalyzer.isLog()) {
//                    if (colorAnalyzer.isLog() && !colorAnalyzer.getValue().equals(colorAnalyzer.getPreviousValue())) {
//                        String csvString = System.currentTimeMillis() + "," +
//                                colorAnalyzer.getName() + "," +
//                                colorAnalyzer.getPreviousValue() +
//                                "," + colorAnalyzer.getValue();
//                        writeToCSV(alarmsFileWriter, csvString);
//                        System.out.println("[LOGGER] Wrote status change");
//                    }
//                }
//            }
//        }
//    }
    private void writeToCSV (FileWriter fw, String str){
        String add = str + "\n";
        try {
            fw.append(add);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
