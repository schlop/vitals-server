package screencapture;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
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

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.cvFont;
import static org.bytedeco.javacpp.opencv_imgproc.cvPutText;


/**
 * Created by Paul on 11/09/2017.
 * <p>
 * Reads the position of the different VitalSignAnalyzers from an XML config file
 * After setup loops over the analyzers and requests vital sign data in an endless loop
 */
public class ScreenCaptureController {
    private IplImage grabbedImage;
    private FrameGrabber grabber;
    private OpenCVFrameConverter.ToIplImage converter;

    private ArrayList<VitalSignAnalyzer> vitalSignAnalyzers;
    private ArrayList<ChartAnalyzer> chartAnalyzers;

    private MainController mainController;

    private void setupScreenCapture() throws Exception {
        grabber = FrameGrabber.createDefault(Integer.parseInt(Config.getInstance().getProp("captureDeviceNumber")));
        grabber.setImageWidth(Integer.parseInt(Config.getInstance().getProp("captureImageWidth")));
        grabber.setImageHeight(Integer.parseInt(Config.getInstance().getProp("captureImageHeight")));
        grabber.start();
        grabbedImage = converter.convert(grabber.grab());
        System.out.println(grabbedImage.width());
        System.out.println(grabbedImage.height());
    }

    public ScreenCaptureController(MainController mainController) {
        //setup of class variables
        this.mainController = mainController;
        Loader.load(opencv_objdetect.class);

        //initialize screen capture
        converter = new OpenCVFrameConverter.ToIplImage();
        if (!Config.getInstance().getProp("debugEnabled").equals("true")) {
            while (true) {
                try {
                    setupScreenCapture();
                    System.out.println("Established Screen Capture");
                    break;
                } catch (Exception e) {
                    System.out.println("Could no establish Screen Capture");
                    e.printStackTrace();
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }
            }
        } else {
            String debugFilePath = Config.getInstance().getProp("sampleImagePath");
            grabbedImage = cvLoadImage(debugFilePath);
        }


        //read config xml and create vital sign analyzer for each field
        chartAnalyzers = new ArrayList<ChartAnalyzer>();
        vitalSignAnalyzers = new ArrayList<VitalSignAnalyzer>();
        try {
            File xml = new File("config.xml");
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();

            NodeList ops = doc.getElementsByTagName("op");
            for (int i = 0; i < ops.getLength(); i++) {
                Node op = ops.item(i);
                int opLabel = Integer.parseInt(op.getAttributes().item(0).getNodeValue());
                NodeList vitalSigns = op.getChildNodes();
                for (int j = 0; j < vitalSigns.getLength(); j++) {
                    Node vitalSign = vitalSigns.item(j);
                    if (vitalSign.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList cords = vitalSign.getChildNodes();
                        Enums.VITAL_SIGN_TYPE vitalSignEnum = Enums.VITAL_SIGN_TYPE.valueOf(vitalSign.getAttributes().item(0).getNodeValue());
                        int posXInt = 0;
                        int posYInt = 0;

                        for (int k = 0; k < cords.getLength(); k++) {
                            Node cord = cords.item(k);
                            if (cord.getNodeType() == Node.ELEMENT_NODE) {
                                if (posXInt == 0) posXInt = Integer.parseInt(cord.getFirstChild().getNodeValue());
                                else posYInt = Integer.parseInt(cord.getFirstChild().getNodeValue());
                            }
                        }
                        if (vitalSignEnum != Enums.VITAL_SIGN_TYPE.CHART) {
                            VitalSign vs = new VitalSign(vitalSignEnum, opLabel);
                            VitalSignAnalyzer vsa = new VitalSignAnalyzer(vs, posXInt, posYInt);
                            vitalSignAnalyzers.add(vsa);
                        } else {
                            ChartAnalyzer ca = new ChartAnalyzer(posXInt, posYInt, opLabel);
                            chartAnalyzers.add(ca);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error during XML parsing");
            e.printStackTrace();
        }
        System.out.println("[SCREEN CAPTURE] Capture started");
    }

    public void run() {
        long start = System.currentTimeMillis();
        IplImage image = grabbedImage.clone();
        ArrayList<VitalSign> vitalSigns = new ArrayList<VitalSign>();
        for (VitalSignAnalyzer vsa : vitalSignAnalyzers) {
            VitalSign vitalSign = vsa.processImage(image);
            vitalSigns.add(vitalSign);
        }
        for (ChartAnalyzer ca : chartAnalyzers) {
            ca.processImage(image);
        }
        mainController.vitalSignUpdate(vitalSigns);
        if (Config.getInstance().getProp("validationEnabled").equals("true")) {
            recordScreen(image, vitalSigns);
        }
        image.release();
        long duration = System.currentTimeMillis() - start;
        System.out.println("[SCREEN CAPTURE] Analyzed vital signs in " + duration + " ms");
    }


    public void start() {
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    if (!Config.getInstance().getProp("debugEnabled").equals("true")) {
                        try {
                            grabber.grab();
                        } catch (FrameGrabber.Exception e) {
                            e.printStackTrace();
                        }
                    }
                    long start = System.currentTimeMillis();
                    IplImage image = grabbedImage.clone();
                    ArrayList<VitalSign> vitalSigns = new ArrayList<VitalSign>();
                    for (VitalSignAnalyzer vsa : vitalSignAnalyzers) {
                        VitalSign vitalSign = vsa.processImage(image);
                        vitalSigns.add(vitalSign);
                    }
                    for (ChartAnalyzer ca : chartAnalyzers) {
                        ca.processImage(image);
                    }
                    mainController.vitalSignUpdate(vitalSigns);
                    if (Config.getInstance().getProp("validationEnabled").equals("true")) {
                        recordScreen(image, vitalSigns);
                    }
                    image.release();
                    long duration = System.currentTimeMillis() - start;
                    System.out.println("[SCREEN CAPTURE] Analyzed vital signs in " + duration + " ms");
                }
            }
        };
        thread.start();
    }


    private void recordScreen(IplImage image, List<VitalSign> vitalSigns) {
        IplImage copyedImage = image.clone();
        CvFont font = cvFont(1, 1);
        for (int i = 0; i < vitalSigns.size(); i++) {
            VitalSign vs = vitalSigns.get(i);
            int[] pos = {vs.getPosx(), vs.getPosy()};
            if (vs.getVitalSignType() != Enums.VITAL_SIGN_TYPE.ALARM_LEVEL1 &&
                    vs.getVitalSignType() != Enums.VITAL_SIGN_TYPE.ALARM_LEVEL2) {
                cvPutText(copyedImage, vs.getValue(), pos, font, opencv_core.CvScalar.WHITE);
            }
        }
        String path = Config.getInstance().getProp("extractedValidationPath") + "/" + System.currentTimeMillis() + ".png";
        cvSaveImage(path, copyedImage);
        copyedImage.release();
    }
}
