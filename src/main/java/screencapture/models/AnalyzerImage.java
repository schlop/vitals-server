package screencapture.models;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.javacv.OpenCVFrameConverter;
import publisher.Publisher;
import screencapture.Config;

import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Base64;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

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
        //TODO: Make this a bit nicer; Check if Publish is set; Allow frame rate limiting to save bandwidth
//        String path = Config.getInstance().getProp("extractedChartPath") + "/" + getName() + ".png";
//        cvSaveImage(path, copiedImage);
        CvMat fu = cvEncodeImage(".png", croppedImage);
        ByteBuffer bb = fu.getByteBuffer();
        byte[] arr = new byte[bb.remaining()];
        bb.get(arr);
        String result = Base64.getEncoder().encodeToString(arr);
        setValue(result);
        System.out.println(result);
        Publisher.INSTANCE.publish(this.toJSON());

        fu.release();
        croppedImage.release();
        copiedImage.release();
    }
}
