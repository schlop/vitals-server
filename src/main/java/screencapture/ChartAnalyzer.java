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
        opencv_core.CvRect cropBox = new opencv_core.CvRect();
        cropBox.x(posx);
        cropBox.y(posy);
        cropBox.width(Config.VITAL_SIGN_TYPE.CHART.getWidth());
        cropBox.height(Config.VITAL_SIGN_TYPE.CHART.getHeight());
        cvSetImageROI(image, cropBox);
        IplImage croppedImage = cvCloneImage(image);
        cvCopy(image, croppedImage);

        String path = Config.CHART_PATH + "/" + op + ".png";
        cvSaveImage(path, croppedImage);
        cvReleaseImage(croppedImage);
    }
}
