package com.testsigma.addons.util;

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
}
