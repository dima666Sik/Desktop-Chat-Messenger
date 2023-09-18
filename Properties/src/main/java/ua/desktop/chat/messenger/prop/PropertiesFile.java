package ua.desktop.chat.messenger.prop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFile {
    private final static Logger logger = LogManager.getLogger(PropertiesFile.class.getName());

    public static Properties getProp(String namePropertiesFile) {
        Properties prop = null;
        try (InputStream propertiesFile = PropertiesFile.class.getClassLoader().getResourceAsStream(namePropertiesFile)){
            prop = new Properties();
            prop.load(propertiesFile);
            logger.info("Properties file was read! Ready props to used in application!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
}