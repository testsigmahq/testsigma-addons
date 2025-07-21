package com.testsigma.addons.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SnowflakeDBConnection {
    public static Statement getConnection(String url) throws Exception {
        Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
        try {
            Connection connection = DriverManager.getConnection(url);

            // Create statement
            Statement statement = connection.createStatement();
            statement.executeQuery("ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON'");
            return statement;
        }
        catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static String P8FileCreator(String privateKey) {
        File p8File = null;
        try {
            // Create a temporary directory
            File tempDir = Files.createTempDirectory("p8-temp-dir").toFile();
            tempDir.deleteOnExit(); // Clean up the directory on JVM exit
            System.out.println("Temporary directory created at: " + tempDir.getAbsolutePath());

            // Create the .p8 file inside the temporary directory
            p8File = new File(tempDir, "AuthKey_Example.p8");
            p8File.deleteOnExit(); // Clean up the file on JVM exit

            String formattedKey = privateKey
                    .replace("-----BEGIN PRIVATE KEY-----", "-----BEGIN PRIVATE KEY-----\n")
                    .replace("-----END PRIVATE KEY-----", "\n-----END PRIVATE KEY-----");

            try (FileWriter writer = new FileWriter(p8File)) {
                writer.write(formattedKey);
            }
        } catch (IOException e) {
            System.err.println("Error creating private key file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating private key file: " + e.getMessage());
        }
        return p8File.getAbsolutePath();
    }
}
