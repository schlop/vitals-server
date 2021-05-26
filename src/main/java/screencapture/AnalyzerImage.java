package screencapture;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

/**
 * Created by Paul on 29/01/2018.
 * Extracts the area of the screen displaying the ECG charts
 * No OCR is required
 *
 */
public class AnalyzerImage extends  Analyzer{
    private int position_x;
    private int position_y;
    private int size_x;
    private int size_y;

    public AnalyzerImage(String name, int position_x, int position_y, int size_x, int size_y) {
        super(name);
        this.position_x = position_x;
        this.position_y = position_y;
        this.size_x = size_x;
        this.size_y = size_y;
    }

    public void processImage(IplImage image) {
        IplImage copiedImage = image.clone();
        opencv_core.CvRect cropBox = new opencv_core.CvRect();
        cropBox.x(position_x);
        cropBox.y(position_y);
        cropBox.width(size_x);
        cropBox.height(size_y);
        cvSetImageROI(copiedImage, cropBox);
        IplImage croppedImage = copiedImage.clone();
        cvCopy(copiedImage, croppedImage);
        //TODO: In this class we do not have to log but always transmit the image; Call communicator image method here
        //String path = Config.getInstance().getProp("extractedChartPath") + "/" + getName() + ".png";
        //cvSaveImage(path, croppedImage);
        croppedImage.release();
        copiedImage.release();
    }
}
