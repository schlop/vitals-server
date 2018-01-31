package screencapture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Config instance = null;
    private Properties prop;

    protected Config() {
        prop = new Properties();
        InputStream input = null;
        try {

            input = new FileInputStream("applicationConfig.txt");

            // load a properties file
            prop.load(input);
        } catch (FileNotFoundException e) {
            System.out.println("[CONFIG] Could not read config file");
        } catch (IOException e) {
            System.out.println("[Config] Could not process config file");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String getProp(String key){
        return prop.getProperty(key);
    }
}
