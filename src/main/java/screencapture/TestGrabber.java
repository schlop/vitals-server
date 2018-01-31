package screencapture;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.opencv_core.*;

public class TestGrabber {
    public static void main(String[] args) throws Exception {


        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);

        // We can "cast" Pointer objects by instantiating a new object of the desired class.

        // The available FrameGrabber classes include OpenCVFrameGrabber (opencv_videoio),
        // DC1394FrameGrabber, FlyCaptureFrameGrabber, OpenKinectFrameGrabber, OpenKinect2FrameGrabber,
        // RealSenseFrameGrabber, PS3EyeFrameGrabber, VideoInputFrameGrabber, and FFmpegFrameGrabber.
//        FrameGrabber grabber = FrameGrabber.createDefault(1);
//        grabber.setImageWidth(1920);
//        grabber.setImageWidth(1080);
//        grabber.start();

        FrameGrabber grabber = FrameGrabber.createDefault(1);
        grabber.setImageWidth(1920);
        grabber.setImageHeight(1080);
        grabber.start();


        // CanvasFrame, FrameGrabber, and FrameRecorder use Frame objects to communicate image data.
        // We need a FrameConverter to interface with other APIs (Android, Java 2D, or OpenCV).
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

        // FAQ about IplImage and Mat objects from OpenCV:
        // - For custom raw processing of data, createBuffer() returns an NIO direct
        //   buffer wrapped around the memory pointed by imageData, and under Android we can
        //   also use that Buffer with Bitmap.copyPixelsFromBuffer() and copyPixelsToBuffer().
        // - To get a BufferedImage from an IplImage, or vice versa, we can chain calls to
        //   Java2DFrameConverter and OpenCVFrameConverter, one after the other.
        // - Java2DFrameConverter also has static copy() methods that we can use to transfer
        //   data more directly between BufferedImage and IplImage or Mat via Frame objects.
        IplImage grabbedImage = converter.convert(grabber.grab());
        int width  = grabbedImage.width();
        int height = grabbedImage.height();
        System.out.println(cvGetSize(grabbedImage));
        System.out.println(width + ","  + height);

        // Objects allocated with a create*() or clone() factory method are automatically released
        // by the garbage collector, but may still be explicitly released by calling release().
        // You shall NOT call cvReleaseImage(), cvReleaseMemStorage(), etc. on objects allocated this way.
        CvMemStorage storage = CvMemStorage.create();

        // CanvasFrame is a JFrame containing a Canvas component, which is hardware accelerated.
        // It can also switch into full-screen mode when called with a screenNumber.
        // We should also specify the relative monitor/camera response for proper gamma correction.
        CanvasFrame frame = new CanvasFrame("Some Title", CanvasFrame.getDefaultGamma()/grabber.getGamma());


        while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {

//            CvRect rect = new CvRect(0,320,1280,1080);
//            IplImage newImage = grabbedImage.clone();
//            cvSetImageROI(newImage, rect);
//            IplImage finalImage = cvCreateImage(cvGetSize(newImage),grabbedImage.depth(), grabbedImage.nChannels());


            cvClearMemStorage(storage);

            Frame finalFrame = converter.convert(grabbedImage);
            frame.showImage(finalFrame);
        }
    }
}