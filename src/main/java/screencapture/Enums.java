package screencapture;

/**
 * Created by Paul on 12/09/2017.
 * Stores the dimensions and allowed characters of vital signs and the colors of different alarm messages
 */
public class Enums {
        public enum ALARM_TYPE{
        NONE, LABEL, NOTIFICATION, WARNING, ALARM;

        private int[] none_rgb = {0, 0, 0};
        private int[] notification_rgb = {77, 255, 255};
        private int[] warning_rgb = {253, 255, 76};
        private int[] alarm_rgb = {240, 8, 37};
        private int[] label_rgb = {255, 255, 255};

        public int[] getRgb(){
            switch (this){
                case NOTIFICATION: return notification_rgb;
                case WARNING: return warning_rgb;
                case ALARM: return alarm_rgb;
                case LABEL: return label_rgb;
            }
            return none_rgb;
        }
    }

    public enum VITAL_SIGN_TYPE {
        HF, SPO2, NBP, ABP, ALARM1, ALARM_LEVEL1, ALARM2, ALARM_LEVEL2, CHART;

        private int hfx = 65;
        private int spo2x = 56;
        private int nbpx = 98;
        private int abpx = 66;
        private int alarm1x = 175;
        private int alarm2x = 175;
        private int chartx = 310;

        private int hfy = 34;
        private int spo2y = 31;
        private int nbpy = 23;
        private int abpy = 51;
        private int alarm1y = 22;
        private int alarm2y = 22;
        private int charty = 121;

        private String hf_allowed = "0123456789-?";
        private String spo2_allowed = "0123456789-?";
        private String nbp_allowed = "0123456789/()-?";
        private String abp_allowed = "0123456789/()-?";
        private String alarm1_allowed = "";
        private String alarm2_allowed = "";

        public int getWidth(){
            switch (this){
                case HF: return hfx;
                case SPO2: return spo2x;
                case NBP: return nbpx;
                case ABP: return abpx;
                case ALARM1: return alarm1x;
                case ALARM2: return alarm2x;
                case CHART: return chartx;
            }
            return 0;
        }

        public int getHeight(){
            switch (this){
                case HF: return hfy;
                case SPO2: return spo2y;
                case NBP: return nbpy;
                case ABP: return abpy;
                case ALARM1: return alarm1y;
                case ALARM2: return alarm2y;
                case CHART: return charty;
            }
            return 0;
        }

        public String getPossibleChars(){
            switch (this){
                case HF: return hf_allowed;
                case SPO2: return spo2_allowed;
                case NBP: return nbp_allowed;
                case ABP: return abp_allowed;
                case ALARM1: return alarm1_allowed;
                case ALARM2: return alarm2_allowed;
            }
            return  "";
        }
    }
}
