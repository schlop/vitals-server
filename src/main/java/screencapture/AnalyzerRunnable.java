package screencapture;

import org.bytedeco.javacpp.opencv_core;

public class AnalyzerRunnable implements Runnable{
    private Analyzer analyzer;
    private opencv_core.IplImage image;

    public AnalyzerRunnable(Analyzer analyzer, opencv_core.IplImage image) {
        this.analyzer = analyzer;
        this.image = image;
    }

    @Override
    public void run() {
        analyzer.processImage(image);
    }
}
