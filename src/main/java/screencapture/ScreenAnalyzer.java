package screencapture;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_imgproc.CvFont;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;


/**
 * Created by Paul on 11/09/2017.
 */
public class ScreenAnalyzer {

    private boolean running;
    private boolean debug;
    private boolean record;
    private boolean firstRun;
    private Timer timer;

    private FrameGrabber grabber;
    private OpenCVFrameConverter.ToIplImage converter;

    private ArrayList<VitalSignAnalyzer> vitalSignAnalyzers;
    private ArrayList<VitalSign> vitalSigns;

    private CvFont font;


    public ScreenAnalyzer() {
        //setup of class variables
        running = false;
        debug = true;
        record = true;
        timer = new Timer();
        firstRun = true;
        vitalSigns = new ArrayList<VitalSign>();
        font = cvFont(1.5, 2);

        Loader.load(opencv_objdetect.class);
        try {
            grabber = FrameGrabber.createDefault(0);
        } catch (Exception e) {
            System.out.println("Could not initialize ScreenGrabber for the selected device");
            System.out.println(e.getMessage());
        }
        converter = new OpenCVFrameConverter.ToIplImage();

        vitalSignAnalyzers = new ArrayList<VitalSignAnalyzer>();


        //read config xml
        try {
            File xml = new File(Config.CONFIG_XML_PATH + "/config.xml");
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();

            NodeList ops = doc.getElementsByTagName("op");
            for (int i = 0; i < ops.getLength(); i++) {
                Node op = ops.item(i);
                NodeList vitalSigns = op.getChildNodes();
                for (int j = 0; j < vitalSigns.getLength(); j++) {
                    Node vitalSign = vitalSigns.item(j);
                    if (vitalSign.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList cords = vitalSign.getChildNodes();

                        Config.VITAL_SIGN_TYPE vitalSignEnum = Config.VITAL_SIGN_TYPE.valueOf(vitalSign.getAttributes().item(0).getNodeValue());
                        int posXInt = 0;
                        int posYInt = 0;

                        for (int k = 0; k < cords.getLength(); k++) {
                            Node cord = cords.item(k);
                            if (cord.getNodeType() == Node.ELEMENT_NODE) {
                                if (posXInt == 0) posXInt = Integer.parseInt(cord.getFirstChild().getNodeValue());
                                else posYInt = Integer.parseInt(cord.getFirstChild().getNodeValue());
                            }
                        }
                        VitalSign vs = new VitalSign(vitalSignEnum, i);
                        VitalSignAnalyzer vsa = new VitalSignAnalyzer(vs, posXInt, posYInt);
                        vitalSignAnalyzers.add(vsa);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error during XML parsing");
            System.out.println(e.getMessage());
        }
    }

    public void start() {
        running = true;
        try {
            grabber.start();
        } catch (Exception e) {
            System.out.println("Could not start screenGrabber");
            System.out.println(e.getMessage());
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (running) {
                    processScreenAnalysis();
                } else {
                    timer.cancel();
                }
            }
        }, 0, 1000);

    }

    public void stop() {
        running = false;
        try {
            grabber.stop();
        } catch (Exception e) {
            System.out.println("Could not stop screenGrabber");
            System.out.println(e.getMessage());
        }
    }

    private void processScreenAnalysis() {
        ArrayList<VitalSign> previousVitalSigns = new ArrayList<VitalSign>(vitalSigns);

        IplImage img = retrieveImage();
        IplImage copy = img.clone();
        analyzeScreen(img);

        if (record && !firstRun) {
            recordScreen(copy, vitalSigns, previousVitalSigns);
        } else {
            firstRun = !firstRun;
        }
        cvReleaseImage(img);
        cvReleaseImage(copy);

    }

    private IplImage retrieveImage() {
        IplImage grabbedImage = null;
        if (debug && grabbedImage == null) {
            String debugFilePath = Config.DEBUG_PICTURE_PATH + "/vitalSignImage.bmp";
            grabbedImage = cvLoadImage(debugFilePath);

        } else if (!debug) {
            try {
                grabbedImage = converter.convert(grabber.grab());
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }
        return grabbedImage;
    }

    private void analyzeScreen(IplImage grabbedImage) {
        vitalSigns = new ArrayList<VitalSign>();
        for (VitalSignAnalyzer vsa : vitalSignAnalyzers) {
            VitalSign vitalSign = vsa.processImage(grabbedImage);
            vitalSigns.add(vitalSign);
        }
    }

    private void recordScreen(IplImage grabbedImage, List<VitalSign> vitalSigns, List<VitalSign> previousVitalSigns) {
        int counter = 0;
        for (int i = 0; i < vitalSigns.size(); i++) {
            VitalSign vs = vitalSigns.get(i);
            VitalSign pvs = previousVitalSigns.get(i);
            if (!vs.equals(pvs)) {
                String text = vs.getValue();
                int[] pos = {vs.getPosx(), vs.getPosy()};
                cvPutText(grabbedImage, text, pos, font, CvScalar.WHITE);
                counter++;
            }
        }
        int[] pos = {10, 1000};
        cvPutText(grabbedImage, String.valueOf(counter), pos , font, CvScalar.WHITE);
        String path = Config.VALIDATION_PATH + "/" + System.currentTimeMillis() + ".jpg";
        cvSaveImage(path, grabbedImage);
    }
}
