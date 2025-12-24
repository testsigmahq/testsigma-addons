package com.testsigma.addons.ios;


import com.testsigma.addons.oracledb.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute OracleDB Query on the Connection DB_Connection_URL and store the result in a variable-name",
        description = "This action executes given query against the connection provided and prints the no. of affected/fetched rows.",
        applicationType = ApplicationType.IOS)
public class Oraclequeries extends IOSAction {

    @TestData(reference = "Query")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "DB_Connection_URL")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        DatabaseUtil databaseUtil = new DatabaseUtil();
        Connection connection = null;
        Statement stmt = null;
        CallableStatement callableStmt = null;
        String query = null;
        try {
            String connectionUrl = testData2.getValue().toString();
            logger.info("Attempting to connect to database. Connection URL: " + maskConnectionUrl(connectionUrl));
            connection = databaseUtil.getConnection(connectionUrl);
            if (connection == null) {
                result = Result.FAILED;
                String errorMsg = "Failed to establish a database connection. Check your DB_Connection_URL.";
                logger.warn(errorMsg);
                setErrorMessage(errorMsg);
                return result;
            }
            logger.info("Database connection established successfully");

            query = testData1.getValue().toString().trim();
            logger.info("Received SQL query: " + query);
            String upper = query.toUpperCase();
            StringBuilder sb = new StringBuilder();

            // Check if it's a stored procedure call
            boolean isProcedure = upper.startsWith("CALL ") ||
                    upper.startsWith("{CALL ") ||
                    upper.startsWith("EXEC ") ||
                    upper.startsWith("EXECUTE ") ||
                    (upper.startsWith("BEGIN") && upper.contains("END"));

            if (isProcedure) {
                // Handle stored procedure using CallableStatement
                logger.info("Detected stored procedure call. Preparing CallableStatement...");
                // Convert EXEC/EXECUTE syntax to Oracle-compatible CALL syntax
                String oracleQuery = convertToOracleCallSyntax(query);
                logger.info("Original query: " + query);
                logger.info("Converted to Oracle CALL syntax: " + oracleQuery);
                callableStmt = connection.prepareCall(oracleQuery);
                logger.info("Executing stored procedure...");
                boolean hasResultSet = callableStmt.execute();
                logger.info("Stored procedure executed. Has result set: " + hasResultSet);

                if (hasResultSet) {
                    // Procedure returned a ResultSet
                    try (ResultSet resultSet = callableStmt.getResultSet()) {
                        if (resultSet == null) {
                            sb.append("Procedure executed successfully. No result set returned.");
                        } else {
                            ResultSetMetaData rsmd = resultSet.getMetaData();
                            int columnNo = rsmd.getColumnCount();

                            sb.append("Successfully Executed Procedure and Resultset is : <br>");

                            // print column names
                            for (int i = 1; i <= columnNo; i++) {
                                sb.append(rsmd.getColumnName(i)).append(", ");
                            }
                            sb.append("<br>");

                            // print rows
                            while (resultSet.next()) {
                                for (int j = 1; j <= columnNo; j++) {
                                    if (j > 1) sb.append(", ");
                                    String columnValue = resultSet.getString(j);
                                    if (resultSet.wasNull()) columnValue = "";
                                    sb.append(columnValue);
                                }
                                sb.append("<br>");
                            }
                        }
                    }
                } else {
                    // Procedure executed but no ResultSet
                    int updateCount = callableStmt.getUpdateCount();
                    sb.append("Procedure executed successfully.");
                    if (updateCount >= 0) {
                        sb.append(" Rows affected: ").append(updateCount);
                    }
                }
            } else if (upper.startsWith("SELECT")) {
                // Handle SELECT queries
                logger.info("Detected SELECT query. Creating statement and executing...");
                stmt = connection.createStatement();
                logger.info("Executing SELECT query: " + query);
                ResultSet resultSet = stmt.executeQuery(query);
                logger.info("SELECT query executed successfully. Processing result set...");
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int columnNo = rsmd.getColumnCount();

                sb.append("Successfully Executed Query and Resultset is : <br>");

                // print column names
                for (int i = 1; i <= columnNo; i++) {
                    sb.append(rsmd.getColumnName(i)).append(", ");
                }
                sb.append("<br>");

                // print rows
                while (resultSet.next()) {
                    for (int j = 1; j <= columnNo; j++) {
                        if (j > 1) sb.append(", ");
                        String columnValue = resultSet.getString(j);
                        if (resultSet.wasNull()) columnValue = "";
                        sb.append(columnValue);
                    }
                    sb.append("<br>");
                }

            } else {
                // INSERT / UPDATE / DELETE / DDL
                logger.info("Detected INSERT/UPDATE/DELETE/DDL statement. Creating statement and executing...");
                stmt = connection.createStatement();
                logger.info("Executing UPDATE/DELETE/DDL query: " + query);
                int count = stmt.executeUpdate(query);
                logger.info("Query executed successfully. Rows affected: " + count);
                sb.append("Query executed successfully. Rows affected: ").append(count);
            }
            
        
                String varName = variableName.getValue().toString().trim();
                String output = sb.toString();
                runTimeData.setKey(varName);
                runTimeData.setValue(output);
                logger.info("Stored query result in runtime variable: " + varName);
            
            
            setSuccessMessage("Successfully Executed Query and Resultset is : " + sb.toString() + " Result stored in runtime variable '"
             + variableName.getValue().toString() + "'");
            logger.info("Execution completed successfully. Result: " + sb.toString());
        } catch (SQLException e) {
            String errorMessage = buildDetailedErrorMessage(e, query);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn("SQL Exception occurred while executing query: " + query);
            logger.warn("SQL Error Code: " + e.getErrorCode() + ", SQL State: " + e.getSQLState());
            logger.warn("Error Message: " + e.getMessage());
            logger.warn("Full stack trace: " + ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            String errorMessage = buildDetailedErrorMessage(e, query);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn("Exception occurred while executing query: " + query);
            logger.warn("Error Message: " + e.getMessage());
            logger.warn("Full stack trace: " + ExceptionUtils.getStackTrace(e));
        } finally {
            try {
                if (callableStmt != null) {
                    callableStmt.close();
                }
            } catch (SQLException e) {
                logger.warn("Error closing callable statement: " + e.getMessage() + e);
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.warn("Error closing statement: " + e.getMessage() + e);
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.warn("Error closing connection: " + e.getMessage() + e);
            }
        }
        return result;
    }

    /**
     * Builds a detailed error message including the SQL query and exception details
     */
    private String buildDetailedErrorMessage(Exception e, String query) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Error executing SQL query.\n");

        if (query != null && !query.isEmpty()) {
            errorMsg.append("SQL Query: ").append(query).append("\n");
        }

        if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            errorMsg.append("SQL Error Code: ").append(sqlEx.getErrorCode()).append("\n");
            errorMsg.append("SQL State: ").append(sqlEx.getSQLState()).append("\n");
        }

        errorMsg.append("Error Message: ").append(e.getMessage()).append("\n");
        errorMsg.append("Stack Trace:\n").append(ExceptionUtils.getStackTrace(e));

        return errorMsg.toString();
    }

    /**
     * Masks sensitive information in connection URL for logging
     */
    private String maskConnectionUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "null";
        }
        // Mask password if present in connection string
        // Pattern: password=xxx or pwd=xxx
        String masked = url.replaceAll("(?i)(password|pwd)=[^;@&]+", "$1=***");
        return masked;
    }

    /**
     * Converts EXEC/EXECUTE syntax to Oracle-compatible CALL syntax
     * Examples:
     *   EXEC procedure_name(...) -> {CALL procedure_name(...)}
     *   EXECUTE procedure_name(...) -> {CALL procedure_name(...)}
     */
    private String convertToOracleCallSyntax(String query) {
        if (query == null || query.isEmpty()) {
            return query;
        }

        String trimmed = query.trim();
        String upper = trimmed.toUpperCase();

        // If already in {CALL ...} format, return as-is
        if (upper.startsWith("{CALL") || upper.startsWith("CALL ")) {
            // Ensure it has braces if it's just CALL
            if (upper.startsWith("CALL ") && !trimmed.startsWith("{")) {
                return "{CALL " + trimmed.substring(5).trim() + "}";
            }
            return trimmed;
        }

        // If already in BEGIN...END format, return as-is
        if (upper.startsWith("BEGIN") && upper.contains("END")) {
            return trimmed;
        }

        // Convert EXEC or EXECUTE to {CALL ...}
        if (upper.startsWith("EXEC ")) {
            String procedureCall = trimmed.substring(5).trim(); // Remove "EXEC "
            return "{CALL " + procedureCall + "}";
        }

        if (upper.startsWith("EXECUTE ")) {
            String procedureCall = trimmed.substring(8).trim(); // Remove "EXECUTE "
            return "{CALL " + procedureCall + "}";
        }

        // If no conversion needed, return original
        return trimmed;
    }
}