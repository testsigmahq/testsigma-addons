package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;

import java.sql.*;

public final class Db2Util {

    private Db2Util() {
    }

    public static Connection connect(String jdbcUrl, String username, String password, Logger logger)
            throws ClassNotFoundException, SQLException {
        String normalizedUrl = normalizeUrl(jdbcUrl);
        logger.info("Connecting to DB2 for z/OS at: " + normalizedUrl);
        Class.forName("com.ibm.db2.jcc.DB2Driver");
        return DriverManager.getConnection(normalizedUrl, username, password);
    }

    /**
     * JCC requires every "key=value" property appended after the database
     * name to end with a semicolon, including the last one - a missing
     * trailing ";" makes DB2Driver.tokenizeURLProperties throw. Testers
     * routinely drop it when typing the URL by hand, so add it back here
     * instead of failing on an otherwise-valid connection string.
     */
    static String normalizeUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return jdbcUrl;
        }
        String trimmed = jdbcUrl.trim();
        int lastSlash = trimmed.lastIndexOf('/');
        boolean hasPropertyTail = lastSlash >= 0 && trimmed.indexOf(':', lastSlash) >= 0;
        if (hasPropertyTail && !trimmed.endsWith(";")) {
            return trimmed + ";";
        }
        return trimmed;
    }

    public static String formatResultSet(ResultSet resultSet) throws SQLException {
        StringBuilder resultText = new StringBuilder();
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();

        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    resultText.append(",");
                }
                String columnValue = resultSet.getString(i);
                resultText.append(resultSet.wasNull() ? "" : columnValue);
            }
            resultText.append(System.lineSeparator());
        }
        return resultText.toString().trim();
    }

    public static void close(Connection connection, Statement stmt, ResultSet resultSet, Logger logger) {
        try {
            if (resultSet != null) resultSet.close();
            if (stmt != null) stmt.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            logger.warn("Error closing DB2 resources: " + e.getMessage());
        }
    }
}
