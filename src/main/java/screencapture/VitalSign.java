package screencapture;

/**
 * Created by Paul on 12/09/2017.
 */
public class VitalSign {

    private Config.VITAL_SIGN_TYPE vitalSignType;
    private int op;
    private String value;

    private int posx;
    private int posy;

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

    public int getPosx() {
        return posx;
    }

    public void setPosx(int posx) {
        this.posx = posx;
    }

    public int getPosy() {
        return posy;
    }

    public void setPosy(int posy) {
        this.posy = posy;
    }

    @Override
    public String toString() {
        return "VitalSign{" +
                "op=" + op +
                ", vitalSignType=" + vitalSignType +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!VitalSign.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final VitalSign other = (VitalSign) obj;
        boolean type = other.getVitalSignType() == this.getVitalSignType();
        boolean op = other.getOp() == this.getOp();
        boolean value = other.getValue() == this.getValue();
        return type && op && value;
    }
}
