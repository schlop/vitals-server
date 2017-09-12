package screencapture;

/**
 * Created by Paul on 12/09/2017.
 */
public class VitalSign {

    private Config.VITAL_SIGN_TYPE vitalSignType;
    private int op;
    private String value;

    public VitalSign(Config.VITAL_SIGN_TYPE vitalSignType, int op) {
        this.vitalSignType = vitalSignType;
        this.op = op;
    }

    public Config.VITAL_SIGN_TYPE getVitalSignType() {
        return vitalSignType;
    }

    public int getOp() {
        return op;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VitalSign{" +
                "op=" + op +
                ", vitalSignType=" + vitalSignType +
                ", value=" + value +
                '}';
    }


}
