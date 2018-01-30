package screencapture;

/**
 * Created by Paul on 12/09/2017.
 */
public class Config {

    //path variables
    public static final String TESSERACT_PATH = "";
    public static final String IMAGE_PATH = "extracted";
    public static final String CHART_PATH = "charts";
    public static final String VALIDATION_PATH = "validation";
    public static final String DEBUG_PICTURE_PATH = "debug";
    public static final String CSV_OUTPUT_PATH = "output";

    public static boolean SAVE_OCR_IMAGES = false;
    public static boolean USE_SCREENSHOT_AS_INPUT = true;
    public static boolean ENABLE_HTTP_SERVER = true;
    public static boolean WRITE_RESULTS = false;

    public static final int CAPTURE_DEVICE = 0;
    public static final int CAPTURE_INTERVAL = 2000;
    public static final int CAPTURE_WIDTH = 1920;
    public static final int CAPTURE_HEIGHT = 1080;

    public static final int HTTP_SERVER_PORT = 9555;

    public static final int NUMBER_OP_THEATERS = 6;

    public enum ALARM_TYPE{
        NONE, NOTIFICATION, WARNING, ALARM;

        private int[] none_rgb = {0, 0, 0};
        private int[] notification_rgb = {55, 255, 255};
        private int[] warning_rgb = {255, 255, 60};
        private int[] alarm_rgb = {255, 25, 55};

        public int[] getRgb(){
            switch (this){
                case NOTIFICATION: return notification_rgb;
                case WARNING: return warning_rgb;
                case ALARM: return alarm_rgb;
            }
            return none_rgb;
        }
    }

    public enum VITAL_SIGN_TYPE {
        HF, SPO2, PULS, BP, BP_MEASURE, ALARM, ALARM_LEVEL, CHART;

        private int hfx = 55;
        private int spo2x = 55;
        private int puls = 55;
        private int bpx = 80;
        private int bp_measurex = 43;
        private int alarmx = 180;
        private int chartx = 290;

        private int hfy = 30;
        private int spo2y = 25;
        private int pulsy = 30;
        private int bpy = 20;
        private int bp_measurey = 17;
        private int alarmy = 19;
        private int charty = 95;

        private String hf_allowed = "0123456789-?";
        private String spo2_allowed = "0123456789-?";
        private String puls_allowed = "0123456789-?";
        private String bp_allowed = "0123456789/()-?";
        private String bp_measure_allowed = "NABP";
        private String alarm_allowed = "";

        public int getWidth(){
            switch (this){
                case HF: return hfx;
                case SPO2: return spo2x;
                case PULS: return puls;
                case BP: return bpx;
                case BP_MEASURE: return bp_measurex;
                case ALARM: return alarmx;
                case CHART: return chartx;
            }
            return 0;
        }

        public int getHeight(){
            switch (this){
                case HF: return hfy;
                case SPO2: return spo2y;
                case PULS: return pulsy;
                case BP: return bpy;
                case BP_MEASURE: return bp_measurey;
                case ALARM: return alarmy;
                case CHART: return charty;
            }
            return 0;
        }

        public String getPossibleChars(){
            switch (this){
                case HF: return hf_allowed;
                case SPO2: return spo2_allowed;
                case PULS: return puls_allowed;
                case BP: return bp_allowed;
                case BP_MEASURE: return bp_measure_allowed;
                case ALARM: return alarm_allowed;
            }
            return  "";
        }
    }
}
