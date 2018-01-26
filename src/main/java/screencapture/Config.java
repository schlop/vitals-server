package screencapture;

/**
 * Created by Paul on 12/09/2017.
 */
public class Config {

    //path variables
    public static final String TESSERACT_PATH = "";
    public static final String IMAGE_PATH = "extracted";
    public static final String VALIDATION_PATH = "validation";
    public static final String DEBUG_PICTURE_PATH = "debug";
    public static final String CSV_OUTPUT_PATH = "output";

    public static boolean SAVE_OCR_IMAGES = false;
    public static boolean USE_SCREENSHOT_AS_INPUT = false;
    public static boolean ENABLE_HTTP_SERVER = true;
    public static boolean WRITE_RESULTS = false;

    public static final int CAPTURE_DEVICE = 0;
    public static final int CAPTURE_INTERVAL = 1000;
    public static final int CAPTURE_WIDTH = 1920;
    public static final int CAPTURE_HEIGHT = 1080;

    public static final int HTTP_SERVER_PORT = 9000;

    public static final int NUMBER_OP_THEATERS = 6;

    public enum ALARM_TYPE{
        NONE, NOTIFICATION, WARNING, ALARM;

        private int[] none_rgb = {0, 0, 0};
        private int[] notification_rgb = {176, 255, 255};
        private int[] warning_rgb = {255, 255, 0};
        private int[] alarm_rgb = {248, 0, 0};

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
        HF, SPO2, STII, BP, BP_MEASURE, ALARM, ALARM_LEVEL;

        private int hfx = 56;
        private int spo2x = 40;
        private int stiix = 40;
        private int bpx = 108;
        private int bp_measurex = 42;
        private int alarmx = 129;
        private int alarm_levelx;

        private int hfy = 39;
        private int spo2y = 21;
        private int stiiy = 22;
        private int bpy = 24;
        private int bp_measurey = 24;
        private int alarmy = 20;
        private int alarm_levely;

        private String hf_allowed = "0123456789";
        private String spo2_allowed = "0123456789";
        private String stii_allowed = "0123456789.-";
        private String bp_allowed = "0123456789/()";
        private String bp_measure_allowed = "NABP";
        private String alarm_allowed = "";
        private String alarm_level_allowed = "";

        public int getWidth(){
            switch (this){
                case HF: return hfx;
                case SPO2: return spo2x;
                case STII: return stiix;
                case BP: return bpx;
                case BP_MEASURE: return bp_measurex;
                case ALARM: return alarmx;
                case ALARM_LEVEL: return alarm_levelx;
            }
            return 0;
        }

        public int getHeight(){
            switch (this){
                case HF: return hfy;
                case SPO2: return spo2y;
                case STII: return stiiy;
                case BP: return bpy;
                case BP_MEASURE: return bp_measurey;
                case ALARM: return alarmy;
                case ALARM_LEVEL: return alarm_levely;
            }
            return 0;
        }

        public String getPossibleChars(){
            switch (this){
                case HF: return hf_allowed;
                case SPO2: return spo2_allowed;
                case STII: return stii_allowed;
                case BP: return bp_allowed;
                case BP_MEASURE: return bp_measure_allowed;
                case ALARM: return alarm_allowed;
                case ALARM_LEVEL: return alarm_level_allowed;
            }
            return  "";
        }
    }
}
