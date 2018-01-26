package screencapture;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;


/**
 * Created by Paul on 11/09/2017.
 */
public class ScreenCaptureController implements Runnable{
    private IplImage grabbedImage;

    private VideoInputFrameGrabber grabber;
    private OpenCVFrameConverter.ToIplImage converter;

    private ArrayList<VitalSignAnalyzer> vitalSignAnalyzers;
    private ArrayList<VitalSign> vitalSigns;

    private MainController mainController;

    private void setupScreenCapture() throws Exception{
        grabber = VideoInputFrameGrabber.createDefault(Config.CAPTURE_DEVICE);
        grabber.setImageWidth(Config.CAPTURE_WIDTH);
        grabber.setImageHeight(Config.CAPTURE_HEIGHT);
        grabber.start();
        grabbedImage = converter.convert(grabber.grab());
    }

    public ScreenCaptureController(MainController mainController) {
        //setup of class variables
        this.mainController = mainController;
        vitalSigns = new ArrayList<VitalSign>();
        Loader.load(opencv_objdetect.class);

        //initialize screen capture
        converter = new OpenCVFrameConverter.ToIplImage();
        if (!Config.USE_SCREENSHOT_AS_INPUT) {
            while (true){
                try {
                    setupScreenCapture();
                    System.out.println("Established Screen Capture");
                    break;
                } catch (Exception e) {
                    System.out.println("Could no establish Screen Capture");
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }
            }
        } else {
            String debugFilePath = Config.DEBUG_PICTURE_PATH + "/vitalSignImage.bmp";
            grabbedImage = cvLoadImage(debugFilePath);
        }


        //read config xml and create vital sign analyzer for each field
        vitalSignAnalyzers = new ArrayList<VitalSignAnalyzer>();
        try {
            File xml = new File("config.xml");
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
            e.printStackTrace();
        }
        System.out.println("[SCREEN CAPTURE] Capture started");
    }

    public void run() {
        //check if previous vital signs are required
        ArrayList<VitalSign> previousVitalSigns = null;
        if (Config.SAVE_OCR_IMAGES && !vitalSigns.isEmpty()){
            previousVitalSigns = new ArrayList<VitalSign>(vitalSigns);
        }

        vitalSigns = new ArrayList<VitalSign>();
        for (VitalSignAnalyzer vsa : vitalSignAnalyzers) {
            IplImage copy = grabbedImage.clone();
            VitalSign vitalSign = vsa.processImage(copy);
            vitalSigns.add(vitalSign);
            cvReleaseImage(copy);
        }

        if (previousVitalSigns != null) {
            recordScreen(grabbedImage, vitalSigns, previousVitalSigns);
        }
        mainController.vitalSignUpdate(vitalSigns);

        System.out.println("[SCREEN CAPTURE] Analyzed vital signs");
    }

    private void recordScreen(IplImage grabbedImage, List<VitalSign> vitalSigns, List<VitalSign> previousVitalSigns) {
//        CvFont font = cvFont(1.5, 2);
//        int counter = 0;
//        for (int i = 0; i < vitalSigns.size(); i++) {
//            VitalSign vs = vitalSigns.get(i);
//            VitalSign pvs = previousVitalSigns.get(i);
//            if (!vs.equals(pvs)) {
//                String text = vs.getValue();
//                int[] pos = {vs.getPosx(), vs.getPosy()};
//                //cvPutText(grabbedImage, text, pos, font, CvScalar.WHITE);
//                counter++;
//            }
//        }
//        int[] pos = {10, 1000};
//        cvPutText(grabbedImage, String.valueOf(counter), pos , font, CvScalar.WHITE);
        String path = Config.VALIDATION_PATH + "/" + System.currentTimeMillis() + ".jpg";
        cvSaveImage(path, grabbedImage);

        System.out.println("[SCREEN CAPTURE] Exported validation image. Changes detected: " + 0);
    }
}
