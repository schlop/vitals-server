package screencapture;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;

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

        if (vitalSign.getVitalSignType() != Config.VITAL_SIGN_TYPE.ALARM_LEVEL) {
            this.ocr = new tesseract.TessBaseAPI();
            if (this.ocr.Init(Config.TESSERACT_PATH, "eng") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }
            if (vitalSign.getVitalSignType() != Config.VITAL_SIGN_TYPE.ALARM)
                this.ocr.SetVariable("tessedit_char_whitelist", vitalSign.getVitalSignType().getPossibleChars());
        }
    }

    public VitalSign processImage(IplImage image) {
        if (vitalSign.getVitalSignType() != Config.VITAL_SIGN_TYPE.ALARM_LEVEL) {
            IplImage adjustedImage = adjustImage(image);
            String path = Config.IMAGE_PATH + "/" + posx + posy + ".tif";
            cvSaveImage(path, adjustedImage);

            BytePointer outText;
            lept.PIX input = pixRead(path);
            ocr.SetImage(input);
            outText = ocr.GetUTF8Text();
            String output = outText.getString();
            output = output.replace("\n", "").replace("\r", "");

            vitalSign.setValue(output);
            return vitalSign;
        } else {
//            CvScalar s = cvGet2D(image, posy, posx);
//            for (Config.ALARM_TYPE config : Config.ALARM_TYPE.values()) {
//                int r = (int) (config.getRgb()[0] - s.val(0));
//                int g = (int) (config.getRgb()[1] - s.val(1));
//                int b = (int) (config.getRgb()[2] - s.val(2));
//                if (r + g + b < 10) {
//                    vitalSign.setValue(config.toString());
//                    return vitalSign;
//                }
//            }
        }
        vitalSign.setValue("unknown");
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

        return resizedImage;
    }
}
