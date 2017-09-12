package screencapture;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;

import static org.bytedeco.javacpp.lept.pixRead;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.tesseract.TessBaseAPI;

/**
 * Created by Paul on 11/09/2017.
 */
public class Screencapture {

    public static void main(String[] args) throws Exception {

        //We could use this code to get video of the capture card and then process it
        //Loader.load(opencv_objdetect.class);
        //FrameGrabber grabber = FrameGrabber.createDefault(0);
        //grabber.start();

        //We use a static image for testing purposes

        getTextFromImage("untitled.bmp");
    }

    private static void getTextFromImage(String imagePath) {
        File imageFile = new File(imagePath);
        String imageFilePath = imageFile.getAbsolutePath();
        System.out.println(imageFilePath);
        opencv_core.IplImage image = cvLoadImage(imageFilePath);

        image = cropImage(image);
        image = cleanImage(image);

        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        CanvasFrame frame = new CanvasFrame("Some Title", CanvasFrame.getDefaultGamma());
        frame.showImage(converter.convert(image));

        cvSaveImage("test.tif", image);

        BytePointer outText;
        TessBaseAPI api = new TessBaseAPI();
        if (api.Init(null, "eng") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }
        File outputFile = new File("test.tif");
        String outputFilePath = outputFile.getAbsolutePath();
        System.out.println(outputFilePath);
        lept.PIX input = pixRead(outputFilePath);
        System.out.println(input);
        api.SetImage(input);

        outText = api.GetUTF8Text();
        String output = outText.getString();
        System.out.println(output);
    }

    private static opencv_core.IplImage cropImage(opencv_core.IplImage image){
        opencv_core.CvRect cropBox = new opencv_core.CvRect();
        cropBox.x(433);
        cropBox.y(188);
        cropBox.width(55);
        cropBox.height(39);

        cvSetImageROI(image, cropBox);
        opencv_core.IplImage cropedImage = cvCloneImage(image);
        cvCopy(image, cropedImage);

        return cropedImage;
    }

    private static opencv_core.IplImage cleanImage(opencv_core.IplImage image){
        opencv_core.IplImage cleanImage = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
        cvCvtColor(image, cleanImage, CV_BGR2GRAY);
        cvThreshold(cleanImage, cleanImage, 0, 255, CV_THRESH_BINARY_INV);

        return cleanImage;
    }
}
