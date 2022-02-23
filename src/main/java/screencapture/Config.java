package screencapture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton class that reads application configuration from a text file
 */
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
            System.out.println("[CONFIG] Application config read");
        } catch (FileNotFoundException e) {
            System.err.println("[CONFIG] Could not read config file");
        } catch (IOException e) {
            System.err.println("[CONFIG] Could not read config file");
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

    public String getProp(String key) {
        return prop.getProperty(key);
    }
}
