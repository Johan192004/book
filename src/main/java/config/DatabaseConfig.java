package config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import util.Logger;

public class DatabaseConfig {
    private final Connection connection = getInstance();

    public Connection getInstance(){
        if(connection != null){
            return connection;
        }
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(
                PropertiesLoad.DB_URL,
                PropertiesLoad.DB_USER,
                PropertiesLoad.DB_PASSWORD
            );
            conn.setAutoCommit(false);
            Logger.info("DatabaseConfig", "Database connection established successfully");

        } catch (SQLException e) {
            Logger.error("DatabaseConfig", "Error establishing database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return conn;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
