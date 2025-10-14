package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoad {
    private final static Properties prop = new Properties();

    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWORD;

    static {
        try (InputStream input = PropertiesLoad.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                throw new IOException("config.properties not found");
            }
            prop.load(input);
            DB_URL = prop.getProperty("db.url");
            DB_USER = prop.getProperty("db.user");
            DB_PASSWORD = prop.getProperty("db.password");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

}
