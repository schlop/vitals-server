package screencapture;

import org.bytedeco.javacpp.opencv_core;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ColorAnalyzer extends Analyzer {

    private boolean log;
    private boolean publish;
    private int positionX;
    private int positionY;
    private ArrayList<Tuple<String, int[]>> translations;

    public ColorAnalyzer(String name, boolean log, boolean publish, int positionX, int positionY, ArrayList<Tuple<String, int[]>> translations, ArrayList<Tuple<String, String>> dependencyStrings) {
        super(name);
        this.log = log;
        this.publish = publish;
        this.positionX = positionX;
        this.positionY = positionY;
        this.translations = translations;
        setDependencyStrings(dependencyStrings);
    }

    public void processImage(opencv_core.IplImage image) {
        boolean process = false;
        if (getDependencies().size() != 0) {
            for (Tuple tuple : getDependencies()) {
                if (tuple.x instanceof TextAnalyzer) {
                    if (((TextAnalyzer) tuple.x).getValue().equals(tuple.y)) {
                        process = true;
                    }
                }
            }
        } else {
            process = true;
        }
        if (process) {
            setPreviousValue(getValue());
            opencv_core.IplImage imageCopy = image.clone();
            ByteBuffer img = imageCopy.getByteBuffer();
            int stepB = positionY * imageCopy.widthStep() + positionX * imageCopy.nChannels() + 0;
            int stepG = positionY * imageCopy.widthStep() + positionX * imageCopy.nChannels() + 1;
            int stepR = positionY * imageCopy.widthStep() + positionX * imageCopy.nChannels() + 2;
            int b = img.get(stepB) & 0xFF;
            int g = img.get(stepG) & 0xFF;
            int r = img.get(stepR) & 0xFF;
            for (Tuple translation : translations) {
                int difB = Math.abs(((int[])translation.y)[2] - b);
                int difG = Math.abs(((int[])translation.y)[1] - g);
                int difR = Math.abs(((int[])translation.y)[0] - r);
                if (difB + difG + difR < 30) {
                    setValue(translation.x.toString());
                    if (Config.getInstance().getProp("consoleOutputEnabled").equals("true")) {
                        String out = "NAME: " + getName() + "; VALUE: " + getValue();
                        System.out.println(out);
                    }
                    imageCopy.release();
                }
            }
        }
    }

    public boolean isLog() {
        return log;
    }

    public boolean isPublish() {
        return publish;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public ArrayList<Tuple<String, int[]>> getTranslations() {
        return translations;
    }
}
