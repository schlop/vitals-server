# This is the version of the server currently used at UKW Wurzburg

To optimise the character recognition tweak the file ```VitalSignAnalyzer```.

From my experience, changing the size of the image to be analysed helps. Apparently font size 12-14 at 300 DPI works
well, but I usually just change the values until I get satisfying results. The following lines of code in the file are
the most important ones for good recognition.

```
//gray
IplImage coloredImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
cvCvtColor(image, coloredImage, CV_BGR2GRAY);

//upscale
IplImage resizedImage = IplImage.create(width * 2, height * 2, coloredImage.depth(), coloredImage.nChannels());
cvResize(coloredImage, resizedImage);

//otsu
cvThreshold(resizedImage, resizedImage, 0, 255, CV_THRESH_OTSU);
```

For more details see:
* https://github.com/bytedeco/javacv
* https://github.com/tesseract-ocr/tesseract
* https://gmartinezgil.wordpress.com/2016/04/16/create-a-receipt-scanner-app-in-java-using-javacv-opencv-and-tesseract-ocr/
