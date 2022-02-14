package screencapture.controllers;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_imgproc.CvFont;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import screencapture.Config;
import screencapture.models.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

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
    private ArrayList<Analyzer> analyzerList;

    private long startTime;
    private long framesAnalysed;
    private long totalFrames;
    private double frameRate;

    private void setupScreenCapture() throws Exception {
        grabber = FrameGrabber.createDefault(Integer.parseInt(Config.getInstance().getProp("captureDeviceNumber")));
        grabber.setImageWidth(Integer.parseInt(Config.getInstance().getProp("captureImageWidth")));
        grabber.setImageHeight(Integer.parseInt(Config.getInstance().getProp("captureImageHeight")));
        grabber.start();
        grabbedImage = converter.convert(grabber.grab());
    }

    private void setupVideoCapture() throws Exception {
        grabber = new OpenCVFrameGrabber(Config.getInstance().getProp("sampleImagePath"));
        grabber.start();
        grabbedImage = converter.convert(grabber.grab());
        totalFrames = ((OpenCVFrameGrabber) grabber).getLengthInFrames();
        frameRate = ((OpenCVFrameGrabber) grabber).getFrameRate();

    }

    public static void stripEmptyElements(Node node)
    {
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().length() == 0) {
                    child.getParentNode().removeChild(child);
                    i--;
                }
            }
            stripEmptyElements(child);
        }
    }

    public ScreenCaptureController() {
        //setup of class variables
        Loader.load(opencv_objdetect.class);

        //initialize screen capture
        converter = new OpenCVFrameConverter.ToIplImage();
        if (!Config.getInstance().getProp("debugEnabled").equals("photo")) {
            while (true) {
                try {
                    if (Config.getInstance().getProp("debugEnabled").equals("video")) {
                        setupVideoCapture();
//                        System.out.println("Established Video Capture");
                        break;
                    } else {
                        setupScreenCapture();
//                        System.out.println("Established Screen Capture");
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("[SCREEN CAPTURE] Could no establish Screen Capture");
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
        analyzerList = new ArrayList<Analyzer>();
        try {
            File xml = new File(Config.getInstance().getProp("analyzerConfig"));
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();

            stripEmptyElements(root);
            NodeList analyserNodeList = root.getChildNodes();

            for (int i = 0; i < analyserNodeList.getLength(); i++) {
                String analyzerType = null;
                String name = null;
                String allowedChars = null;
                Boolean log = null;
                Boolean publish = null;
                Integer position_x = null;
                Integer position_y = null;
                Integer size_x = null;
                Integer size_y = null;
                ArrayList<Tuple<String, String>> dependencyStrings = new ArrayList<Tuple<String, String>>();
                ArrayList<Tuple<String, int[]>> translations = new ArrayList<Tuple<String, int[]>>();

                Node analyserNode = analyserNodeList.item(i);
                analyzerType = analyserNode.getNodeName();
                name = analyserNode.getAttributes().item(0).getNodeValue();
                NodeList analyserAttributeList = analyserNode.getChildNodes();
                for (int j = 0; j < analyserAttributeList.getLength(); j++) {
                    Node attribute = analyserAttributeList.item(j);
                    switch (attribute.getNodeName()) {
                        case "log":
                            log = Boolean.parseBoolean(attribute.getFirstChild().getNodeValue());
                            break;
                        case "publish":
                            publish = Boolean.parseBoolean(attribute.getFirstChild().getNodeValue());
                            break;
                        case "chars":
                            allowedChars = attribute.getFirstChild().getNodeValue();
                            break;
                        case "dimensions":
                            NodeList positionAttributeList = attribute.getChildNodes();
                            for (int k = 0; k < positionAttributeList.getLength(); k++) {
                                Node positionAttribute = positionAttributeList.item(k);
                                if (positionAttribute.getNodeName() == "position_x")
                                    position_x = Integer.parseInt(positionAttribute.getFirstChild().getNodeValue());
                                else if (positionAttributeList.item(k).getNodeName() == "position_y")
                                    position_y = Integer.parseInt(positionAttribute.getFirstChild().getNodeValue());
                                else if (positionAttributeList.item(k).getNodeName() == "size_x")
                                    size_x = Integer.parseInt(positionAttribute.getFirstChild().getNodeValue());
                                else if (positionAttributeList.item(k).getNodeName() == "size_y")
                                    size_y = Integer.parseInt(positionAttribute.getFirstChild().getNodeValue());
                            }
                            break;
                        case "dependencies":
                            NodeList dependenciesList = attribute.getChildNodes();
                            for (int k = 0; k < dependenciesList.getLength(); k++) {
                                NodeList conditionsList = dependenciesList.item(k).getChildNodes();
                                String[] dependency = new String[2];
                                for (int l = 0; l < conditionsList.getLength(); l++) {
                                    Node conditionAttribute = conditionsList.item(l);
                                    if (conditionAttribute.getNodeName() == "analyser")
                                        dependency[0] = conditionAttribute.getFirstChild().getNodeValue();
                                    else if (conditionAttribute.getNodeName() == "value")
                                        dependency[1] = conditionAttribute.getFirstChild().getNodeValue();
                                }
                                dependencyStrings.add(new Tuple(dependency[0], dependency[1]));
                            }
                            break;
                        case "translations":
                            NodeList colorAttributeList = attribute.getChildNodes();
                            for (int k = 0; k < colorAttributeList.getLength(); k++) {
                                String keyword = colorAttributeList.item(k).getAttributes().item(0).getNodeValue();
                                int[] rgb = new int[3];
                                NodeList rgbAttributeList = colorAttributeList.item(k).getChildNodes();
                                for (int l = 0; l < rgbAttributeList.getLength(); l++) {
                                    Node rgbAttribute = rgbAttributeList.item(l);
                                    if (rgbAttribute.getNodeName() == "r") {
                                        rgb[0] = Integer.parseInt(rgbAttribute.getFirstChild().getNodeValue());
                                    } else if (rgbAttribute.getNodeName() == "g") {
                                        rgb[1] = Integer.parseInt(rgbAttribute.getFirstChild().getNodeValue());
                                    } else if (rgbAttribute.getNodeName() == "b") {
                                        rgb[2] = Integer.parseInt(rgbAttribute.getFirstChild().getNodeValue());
                                    }
                                }
                                Tuple<String, int[]> translation = new Tuple<String, int[]>(keyword, rgb);
                                translations.add(translation);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown field: " + attribute.getNodeName());
                    }
                }
                switch (analyzerType){
                    case "textAnalyser":
                        if (name != null &&
                                log != null &&
                                publish != null &&
                                allowedChars != null &&
                                position_x != null &&
                                position_y != null &&
                                size_x != null &&
                                size_y != null){
                            AnalyzerText analyzerText = new AnalyzerText(name, log, publish, allowedChars, position_x, position_y, size_x, size_y, dependencyStrings);
                            analyzerList.add(analyzerText);
                        }
                        else{
                            throw new IllegalArgumentException("Tried to create TextAnalyser but not all required fields were defined in XML file");
                        }
                        break;
                    case "colorAnalyser":
                        if (name != null &&
                                log != null &&
                                publish != null &&
                                position_x != null &&
                                position_y != null &&
                                translations.size() != 0) {
                            AnalyzerColor analyzerColor = new AnalyzerColor(name, log, publish, position_x, position_y, translations, dependencyStrings);
                            analyzerList.add(analyzerColor);
                        }
                        else{
                            throw new IllegalArgumentException("Tried to create ColorAnalyser but not all required fields were defined in XML file");
                        }
                        break;
                    case "imageAnalyser":
                        if (name != null &&
                                position_x != null &&
                                position_y != null &&
                                size_x != null &&
                                size_y != null){
                            AnalyzerImage analyzerImage = new AnalyzerImage(name, position_x, position_y, size_x, size_y);
                            analyzerList.add(analyzerImage);
                        }
                        else{
                            throw new IllegalArgumentException("Tried to create ImageAnalyser but not all required fields were defined in XML file");
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown analyzer: " + analyzerType);
                }
            }
        } catch (Exception e) {
            System.out.println("Error during XML parsing");
            e.printStackTrace();
        }
        for (Analyzer analyzer : analyzerList){
            for (Tuple<String, String> tuple : analyzer.getDependencyStrings()){
                for (Analyzer compare : analyzerList){
                    if (tuple.x.equals(compare.getName())){
                        analyzer.setDependencies(new Tuple<Analyzer, String>(compare, tuple.y));
                    }
                }
            }
        }
        System.out.println("[SCREEN CAPTURE] Capture started");
    }

    long frameStartTime = 0;
    long frameDuration = 0;

    //TODO replace this with ThreadPoolExecutor; Check if new Frame is available; Then check if ThreadPool is empty; Then cue all analysis steps
    public void start() {
        startTime = System.currentTimeMillis();
        framesAnalysed = 0;
        if (Config.getInstance().getProp("debugEnabled").equals("video")) {
            frameDuration = (long) (1000 / frameRate);
        }
        CanvasFrame canvas = new CanvasFrame("Video Feed");
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    frameStartTime = System.currentTimeMillis();
                    if (!Config.getInstance().getProp("debugEnabled").equals("photo")) {
                        try {
                            grabber.grab();
                        } catch (FrameGrabber.Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //TODO: Dirty hack to loop. Needs some work
                    if (Config.getInstance().getProp("debugEnabled").equals("video")) {
                        if (grabber.getFrameNumber() > totalFrames) {
                            try {
                                grabber.setTimestamp(0);
                                System.out.println("[SCREEN CAPTURE] Restarted video");

                            } catch (FrameGrabber.Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    IplImage image = grabbedImage.clone();
                    for (Analyzer analyzer : analyzerList) {
                        analyzer.processImage(image);
                    }
                    if (Config.getInstance().getProp("validationEnabled").equals("true")) {
                        recordScreen(image);
                    }
                    image.release();
                    framesAnalysed += 1;
                    if (startTime + 60000 < System.currentTimeMillis()){
                        double FPS = framesAnalysed / 60;
                        System.out.println("[SCREEN CAPTURE] Currently analysing " + FPS + " frames per second");
                        framesAnalysed = 0;
                        startTime = System.currentTimeMillis();
                    }
                    canvas.showImage(converter.convert(grabbedImage));
                    long remainingFrameTime = frameDuration - (System.currentTimeMillis() - frameStartTime);
                    if (remainingFrameTime > 0) {
                        try {
                            Thread.sleep(remainingFrameTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        thread.start();
    }

    private void recordScreen(IplImage image) {
        IplImage copyedImage = image.clone();
        CvFont font = cvFont(1, 1);
        for (Analyzer analyzer : analyzerList) {
            if (analyzer instanceof AnalyzerText) {
                AnalyzerText analyzerText = (AnalyzerText) analyzer;
                int[] pos = {analyzerText.getPositionX(), analyzerText.getPositionY()};
                cvPutText(copyedImage, analyzerText.getValue(), pos, font, opencv_core.CvScalar.WHITE);
            }
            else if (analyzer instanceof AnalyzerColor) {
                AnalyzerColor analyzerColor = (AnalyzerColor) analyzer;
                int[] pos = {analyzerColor.getPositionX(), analyzerColor.getPositionY()};
                cvPutText(copyedImage, analyzerColor.getValue(), pos, font, opencv_core.CvScalar.WHITE);
            }
        }
        String path = Config.getInstance().getProp("extractedValidationPath") + "/" + System.currentTimeMillis() + ".png";
        cvSaveImage(path, copyedImage);
        copyedImage.release();
    }

    public void activateTransmission(){
        analyzerList.forEach(analyzer -> analyzer.setActivated(true));
    }

    public void deactivateTransmission(){
        analyzerList.forEach(analyzer -> analyzer.setActivated(false));
    }
}
