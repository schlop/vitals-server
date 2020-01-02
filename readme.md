# Functionality

The UKW Server Application extracts and digitalises alphanumeric characters from screenshots via OCR. The screenshots can be loaded from locally stored files or from an attached screen grabber at a frequent interval. The digital alphanumerics (e.g. vital signs) can be accessed via a https web socket. Data is exchanged as a JSON object.

The application uses Maven to resolve dependencies. The OCR and image processing library used to extract the vital signs is [JavaCV](https://github.com/bytedeco/javacv).

#Overview of the program structure

##applicationConfig.txt
This files contains the general config of the application. In the first block, paths have to be defined. For the application to work properly, folders with the names 'extracted', 'charts', 'validation', 'output', and 'debug' have to be created in the root folder of the application.

In the second block, the application can be configured. 'validationEnabled' is a functionality that saves each captured screenshot with the vital sign readings superimposed in a file. This can be useful to validate how accurate the OCR works. 'debugEnabled' reads the screenshot from a file instead of from the screen grabber. This can be useful when working from home without access to the signal to be captured. 'httpsEnabled' activates the https socket so that vital signs can be accessed from another device (e.g. the HWD). Note that if 'httpsEnabled' is set to false the network component is disabled completely and vital signs cannot be accessed via http either. 'auth' makes data available via the address 'SERVER_IP:PORT/secret_hash' (if set to true) or via 'SERVER_IP:PORT/get' (if set to false).

In the third block, capture frequency, capture device number (is usually 0, 1, or 2 depending on whether a webcam is attached to the computer), and resolution of the screenshot.


##config.xml
This file contains information about the position and name of vital signs. The following example would define a hospital with a single operating room '0', where a single vital sign 'HF' is collected, that is displayed on the screenshot at position 'x: 100, y: 100'.

```xml
<hospital>
	<op number = "0">
		<vitalsign category="HF">
			<postionx>100</postionx>
			<postiony>100</postiony>
		</vitalsign>
  </op>
</hospital>
```
