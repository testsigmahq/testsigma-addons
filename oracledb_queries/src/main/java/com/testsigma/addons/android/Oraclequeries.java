package com.testsigma.addons.android;


import com.testsigma.addons.oracledb.util.DatabaseUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute OracleDB Query on the Connection DB_Connection_URL",
        description = "This action executes given query against the connection provided and prints the no. of affected/fetched rows.",
        applicationType = ApplicationType.ANDROID)
public class Oraclequeries extends AndroidAction {

    @TestData(reference = "Query")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "DB_Connection_URL")
    private com.testsigma.sdk.TestData testData2;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        DatabaseUtil databaseUtil = new DatabaseUtil();
        Connection connection = null;
        Statement stmt = null;
        CallableStatement callableStmt = null;
        try {
            connection = databaseUtil.getConnection(testData2.getValue().toString());
            if (connection == null) {
                result = Result.FAILED;
                setErrorMessage("Failed to establish a database connection. Check your DB_Connection_URL.");
                return result;
            }

            String query = testData1.getValue().toString().trim();
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
                callableStmt = connection.prepareCall(query);
                boolean hasResultSet = callableStmt.execute();

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
                stmt = connection.createStatement();
                ResultSet resultSet = stmt.executeQuery(query);
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
                stmt = connection.createStatement();
                int count = stmt.executeUpdate(query);
                sb.append("Query executed successfully. Rows affected: ").append(count);
            }
            setSuccessMessage(sb.toString());
            logger.info(sb.toString());
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
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
}