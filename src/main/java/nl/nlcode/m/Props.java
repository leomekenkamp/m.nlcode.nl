package nl.nlcode.m;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author leo
 */
public class Props {
    
    private static Props instance;
    
    private String version;
    
    private Props() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("version.properties");) {
            Properties properties = new Properties();
            properties.load(in);
            version = properties.getProperty("version");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public static Props getInstance() {
        if (instance == null) {;
            instance = new Props();
        }
        return instance;
    }
    
    public String getVersion() {
        return version;
    }
}
