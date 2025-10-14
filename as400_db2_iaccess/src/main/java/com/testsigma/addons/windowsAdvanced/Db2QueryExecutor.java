package com.testsigma.addons.windowsAdvanced;
import java.sql.*;
import java.util.Scanner;

public class Db2QueryExecutor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ✅ Get user inputs
        String host = "localhost";
        String port = "50000";
        String dbName = "testdb";
        String user = "db2inst1";
        String password = "password";
        String query = "select name from MYTABLE where id=2 or id=3";

        // ✅ JDBC URL
        // String url = "jdbc:as400://myibmi.example.com/testdb";
//        String url = "jdbc:as400://" + host + ":" + port + "/" + dbName;
         String url = "jdbc:db2://" + host + ":" + port + "/" + dbName;

        System.out.println("\nConnecting to DB2 at: " + url);
        try {
            // ✅ Load DB2 JDBC Driver
            Class.forName("com.ibm.db2.jcc.DB2Driver");

            // ✅ Get connection
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                System.out.println("\nQuery executed successfully! Results:\n");
                // Print rows
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.printf(rs.getString(i));
                    }
                    System.out.println();
                }
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ DB2 Driver not found. Make sure db2jcc4.jar is in classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
