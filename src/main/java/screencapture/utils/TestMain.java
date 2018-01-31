package screencapture.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

/**
 * Created by Paul on 31/01/2018.
 */
public class TestMain {

    public static void main(String[] args) {
        new TestMain();
    }

    String jsonFilePath = "test/";
    String jsonStandardFile = jsonFilePath + "0.json";
    TestServer server;

    public TestMain(){
        server = new TestServer(jsonStandardFile);
        processInputs();
    }

    public void processInputs() {
        System.out.println("Enter JSON file number");
        Scanner keyboard = new Scanner(System.in);
        int in = keyboard.nextInt();
        File file = null;
        file = new File(jsonFilePath + in + ".json");
        try  {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String jsonString = "";
            String line;
            while ((line = br.readLine()) != null) {
                jsonString += line ;
            }
            br.close();
            server.updateFile(jsonString);
        } catch (Exception e) {
            System.out.println("File could not be read. Please enter a valid file number.");
        }
        processInputs();
    }
}
