package screencapture;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;


/**
 * Created by Paul on 12/09/2017.
 * <p>
 * Analyzes a small area of the screen with OCR and extracts characters. For each numeric or word an instance of this
 * class is created.
 */
public class TextAnalyzer extends Analyzer {

    private tesseract.TessBaseAPI ocr;

    private boolean log;
    private boolean publish;
    private String allowedChars;
    private int positionX;
    private int positionY;
    private int sizeX;
    private int sizeY;

    public TextAnalyzer(String name,
                        boolean log,
                        boolean publish,
                        String allowedChars,
                        int position_x,
                        int position_y,
                        int size_x,
                        int size_y,
                        ArrayList<Tuple<String, String>> dependencyStrings) {
        super(name);
        this.log = log;
        this.publish = publish;
        this.allowedChars = allowedChars;
        this.positionX = position_x;
        this.positionY = position_y;
        this.sizeX = size_x;
        this.sizeY = size_y;
        setDependencyStrings(dependencyStrings);
        prepareOCR();
    }

    private void prepareOCR(){
        ocr = new tesseract.TessBaseAPI();
        if (ocr.Init("", "eng") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }
        ocr.SetPageSegMode(6);
        ocr.SetVariable("tessedit_char_whitelist", allowedChars);
    }

    public void processImage(IplImage image) {
        boolean process = false;
        if (getDependencies().size() != 0){
            for (Tuple tuple : getDependencies()){
                if (tuple.x instanceof TextAnalyzer){
                    if (((TextAnalyzer) tuple.x).getValue().equals(tuple.y)){
                        process = true;
                    }
                }
                else if (tuple.x instanceof ColorAnalyzer){
                    if (((ColorAnalyzer) tuple.x).getValue().equals(tuple.y)){
                        process = true;
                    }
                }
            }
        }
        else{
            process = true;
        }
        if (process){
            setPreviousValue(getValue());
            IplImage imageCopy = image.clone();
            IplImage adjustedImage = adjustImage(imageCopy);
            String path = Config.getInstance().getProp("extractedImagePath") + "/" + getName() + ".png";
            cvSaveImage(path, adjustedImage);
            adjustedImage.release();
            BytePointer outText;
            lept.PIX input = pixRead(path);
            ocr.SetImage(input);
            outText = ocr.GetUTF8Text();
            String output = outText.getString();
            setValue(output.replace("\n", "").replace("\r", "").replace(" ", ""));
            pixDestroy(input);

            if (Config.getInstance().getProp("consoleOutputEnabled").equals("true")) {
                String out = "NAME: " + getName() + "; VALUE: " + getValue();
                System.out.println(out);
            }
            imageCopy.release();
        }
    }

    private IplImage adjustImage(IplImage image) {
        //crop
        CvRect cropBox = new CvRect();
        cropBox.x(positionX);
        cropBox.y(positionY);
        cropBox.width(sizeX);
        cropBox.height(sizeY);
        cvSetImageROI(image, cropBox);
        IplImage croppedImage = image.clone();

        //gray
        IplImage coloredImage = IplImage.create(sizeX, sizeY, IPL_DEPTH_8U, 1);
        cvCvtColor(image, coloredImage, CV_BGR2GRAY);

        //upscale
        IplImage resizedImage = IplImage.create((int) (sizeX * 1), (int) (sizeY * 1), coloredImage.depth(), coloredImage.nChannels());
        cvResize(coloredImage, resizedImage);

        //otsu
        cvThreshold(resizedImage, resizedImage, 0, 255, CV_THRESH_OTSU);


        //clone
        IplImage returnImage = resizedImage.clone();

        croppedImage.release();
        coloredImage.release();
        resizedImage.release();

        return returnImage;
    }

    public boolean isLog() {
        return log;
    }

    public boolean isPublish() {
        return publish;
    }

    public String getAllowedChars() {
        return allowedChars;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }
}
