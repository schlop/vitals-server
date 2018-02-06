package screencapture;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

/**
 * Created by Paul on 29/01/2018.
 */
public class ChartAnalyzer {
    int posx;
    int posy;
    int op;

    public ChartAnalyzer(int posXInt, int posYInt, int op) {
        posx = posXInt;
        posy = posYInt;
        this.op = op;
    }

    public void processImage(IplImage image) {
        IplImage copiedImage = image.clone();
        opencv_core.CvRect cropBox = new opencv_core.CvRect();
        cropBox.x(posx);
        cropBox.y(posy);
        cropBox.width(Enums.VITAL_SIGN_TYPE.CHART.getWidth());
        cropBox.height(Enums.VITAL_SIGN_TYPE.CHART.getHeight());
        cvSetImageROI(copiedImage, cropBox);
        IplImage croppedImage = copiedImage.clone();
        cvCopy(copiedImage, croppedImage);

        String path = Config.getInstance().getProp("extractedChartPath") + "/" + op + ".png";
        cvSaveImage(path, croppedImage);
        croppedImage.release();
        copiedImage.release();
    }
}
