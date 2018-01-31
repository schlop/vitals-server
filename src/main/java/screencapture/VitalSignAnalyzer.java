package screencapture;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;

import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;


/**
 * Created by Paul on 12/09/2017.
 */
public class VitalSignAnalyzer {

    private VitalSign vitalSign;
    private int posx;
    private int posy;
    private tesseract.TessBaseAPI ocr;

    public VitalSignAnalyzer(VitalSign vitalSign, int posx, int posy) {

        this.vitalSign = vitalSign;
        this.posx = posx;
        this.posy = posy;

        vitalSign.setPosx(posx);
        vitalSign.setPosy(posy);

        if (vitalSign.getVitalSignType() != Enums.VITAL_SIGN_TYPE.ALARM_LEVEL) {
            this.ocr = new tesseract.TessBaseAPI();
            if (this.ocr.Init("", "eng") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }
            if (vitalSign.getVitalSignType() != Enums.VITAL_SIGN_TYPE.ALARM)
                this.ocr.SetVariable("tessedit_char_whitelist", vitalSign.getVitalSignType().getPossibleChars());
        }
    }

    public VitalSign processImage(IplImage image) {
        if (vitalSign.getVitalSignType() != Enums.VITAL_SIGN_TYPE.ALARM_LEVEL) {
            IplImage adjustedImage = adjustImage(image);
            String path = Config.getInstance().getProp("extractedImagePath") + "/" + vitalSign.getOp() + vitalSign.getVitalSignType() + ".png";
            cvSaveImage(path, adjustedImage);
            BytePointer outText;
            lept.PIX input = pixRead(path);
            ocr.SetImage(input);
            outText = ocr.GetUTF8Text();
            String output = outText.getString();
            output = output.replace("\n", "").replace("\r", "");

            vitalSign.setValue(output);
            outText.close();
            pixDestroy(input);

            if (Config.getInstance().getProp("consoleOutputEnabled").equals("true")) {
                String out = "OP: " + vitalSign.getOp() + "; VS: " + vitalSign.getVitalSignType().toString() + "; VALUE: " + vitalSign.getValue();
                System.out.println(out);
            }
            cvReleaseImage(image);
            return vitalSign;
        } else {
            ByteBuffer img = image.getByteBuffer();
            int stepB = posy * image.widthStep() + posx * image.nChannels() + 0;
            int stepG = posy * image.widthStep() + posx * image.nChannels() + 1;
            int stepR = posy * image.widthStep() + posx * image.nChannels() + 2;
            int b = img.get(stepB) & 0xFF;
            int g = img.get(stepG) & 0xFF;
            int r = img.get(stepR) & 0xFF;
            for (Enums.ALARM_TYPE config : Enums.ALARM_TYPE.values()) {
                int difB = Math.abs(config.getRgb()[2] - b);
                int difG = Math.abs(config.getRgb()[1] - g);
                int difR = Math.abs(config.getRgb()[0] - r);
                if (difB + difG + difR < 20) {
                    vitalSign.setValue(config.toString());
                    if (Config.getInstance().getProp("consoleOutputEnabled").equals("true")) {
                        String out = "OP: " + vitalSign.getOp() + "; VS: " + vitalSign.getVitalSignType().toString() + "; VALUE: " + vitalSign.getValue();
                        System.out.println(out);
                    }
                    cvReleaseImage(image);
                    return vitalSign;
                }
            }
        }
        vitalSign.setValue("unknown");
        if (Config.getInstance().getProp("consoleOutputEnabled").equals("true")) {
            String out = "OP: " + vitalSign.getOp() + "; VS: " + vitalSign.getVitalSignType().toString() + "; VALUE: " + vitalSign.getValue();
            System.out.println(out);
        }
        cvReleaseImage(image);
        return vitalSign;
    }


    private IplImage adjustImage(IplImage image) {
        //crop
        CvRect cropBox = new CvRect();
        cropBox.x(posx);
        cropBox.y(posy);
        cropBox.width(vitalSign.getVitalSignType().getWidth());
        cropBox.height(vitalSign.getVitalSignType().getHeight());
        cvSetImageROI(image, cropBox);
        IplImage croppedImage = cvCloneImage(image);
        cvCopy(image, croppedImage);

        //gray
        IplImage coloredImage = cvCreateImage(cvGetSize(croppedImage), IPL_DEPTH_8U, 1);
        cvCvtColor(image, coloredImage, CV_BGR2GRAY);

        //upscale
        IplImage resizedImage = IplImage.create(coloredImage.width() * 4, coloredImage.height() * 4, coloredImage.depth(), coloredImage.nChannels());
        cvResize(coloredImage, resizedImage);

        //otsu
        cvThreshold(resizedImage, resizedImage, 0, 255, CV_THRESH_OTSU);

        //clone
        IplImage returnImage = resizedImage.clone();

        //release
        cvReleaseImage(croppedImage);
        cvReleaseImage(coloredImage);
        cvReleaseImage(resizedImage);
        cvReleaseImage(image);

        return returnImage;
    }
}
