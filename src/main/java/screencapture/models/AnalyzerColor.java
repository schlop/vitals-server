package screencapture.models;

import org.bytedeco.javacpp.opencv_core;
import publisher.Publisher;
import screencapture.Config;
import screencapture.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AnalyzerColor extends Analyzer {

    private boolean log;
    private boolean publish;
    private int positionX;
    private int positionY;
    private ArrayList<Tuple<String, int[]>> colorNames;

    public AnalyzerColor(String name, boolean log, boolean publish, int positionX, int positionY, ArrayList<Tuple<String, int[]>> colorNames, ArrayList<Tuple<String, String>> dependencyStrings) {
        super(name);
        this.log = log;
        this.publish = publish;
        this.positionX = positionX;
        this.positionY = positionY;
        this.colorNames = colorNames;
        setDependencyStrings(dependencyStrings);
    }

    public void processImage(opencv_core.IplImage image) {
        boolean process = false;
        if (getDependencies().size() != 0) {
            for (Tuple tuple : getDependencies()) {
                if (tuple.x instanceof AnalyzerText) {
                    if (((AnalyzerText) tuple.x).getValue().equals(tuple.y)) {
                        process = true;
                    }
                }
            }
        } else {
            process = true;
        }
        if (process) {
            opencv_core.IplImage imageCopy = image.clone();
            ByteBuffer img = imageCopy.getByteBuffer();
            int stepB = positionY * imageCopy.widthStep() + positionX * imageCopy.nChannels() + 0;
            int stepG = positionY * imageCopy.widthStep() + positionX * imageCopy.nChannels() + 1;
            int stepR = positionY * imageCopy.widthStep() + positionX * imageCopy.nChannels() + 2;
            int b = img.get(stepB) & 0xFF;
            int g = img.get(stepG) & 0xFF;
            int r = img.get(stepR) & 0xFF;
            for (Tuple colorName : colorNames) {
                int difB = Math.abs(((int[]) colorName.y)[2] - b);
                int difG = Math.abs(((int[]) colorName.y)[1] - g);
                int difR = Math.abs(((int[]) colorName.y)[0] - r);
                String result = "unidentified Color";
                if (difB + difG + difR < 30) {
                    result = colorName.x.toString();
                }
                //check now if the value has changed
                if (isActivated() && !result.equals(getValue())) {
                    if (Config.getInstance().getProp("consoleOutputEnabled").equals("true")) {
                        String out = "NAME: " + getName() + "; VALUE: " + getValue();
                        System.out.println(out);
                    }
                    if(publish){
                        Publisher.INSTANCE.publish(this.toJSON());
                    }
                    if(log && Config.getInstance().getProp("vitalSignLogEnabled").equals("true")){
                        Logger.getInstance().log(getName(), result);
                    }
                    setValue(result);
                }
                imageCopy.release();
            }
        }
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public ArrayList<Tuple<String, int[]>> getColorNames() {
        return colorNames;
    }
}
