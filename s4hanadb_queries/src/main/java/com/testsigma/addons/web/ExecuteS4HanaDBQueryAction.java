package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(
        actionText = "Execute S4 HANA DB query query-val on host host-name, port port-no using username username-val and password password-val, store results in runtime variable variable-name",
        description = "Connects to SAP HANA Cloud using JDBC, executes a SQL query, and returns JSON formatted results",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class ExecuteS4HanaDBQueryAction extends WebAction {

    @TestData(reference = "host-name")
    private com.testsigma.sdk.TestData host;

    @TestData(reference = "port-no")
    private com.testsigma.sdk.TestData port;

    @TestData(reference = "username-val")
    private com.testsigma.sdk.TestData username;

    @TestData(reference = "password-val")
    private com.testsigma.sdk.TestData password;

    @TestData(reference = "query-val")
    private com.testsigma.sdk.TestData query;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating SAP HANA query execution");

        String hostValue = host.getValue().toString();
        String portValue = port.getValue().toString();
        String usernameValue = username.getValue().toString();
        String passwordValue = password.getValue().toString();
        String queryValue = query.getValue().toString();

        logger.info("Connecting to SAP HANA - Host: " + hostValue + ", Port: " + portValue +
                ", Username: " + usernameValue);
        logger.info("Query: " + queryValue);

        Connection connection = null;

        try {
            Class.forName("com.sap.db.jdbc.Driver").getDeclaredConstructor().newInstance();

            String jdbcUrl = "jdbc:sap://" + hostValue + ":" + portValue +
                    "?encrypt=true" +
                    "&validateCertificate=false" +
                    "&sslHostNameInCertificate=*";

            connection = DriverManager.getConnection(jdbcUrl, usernameValue, passwordValue);
            logger.info("Connected to SAP HANA!");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(queryValue);

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<String> jsonRows = new ArrayList<>();

            while (resultSet.next()) {
                StringBuilder jsonObj = new StringBuilder();
                jsonObj.append("{");

                for (int i = 1; i <= columnCount; i++) {
                    String colName = metaData.getColumnName(i);
                    Object colValue = resultSet.getObject(i);

                    jsonObj.append("\"").append(colName).append("\": ");

                    if (colValue == null) {
                        jsonObj.append("null");
                    } else {
                        jsonObj.append("\"").append(colValue.toString()).append("\"");
                    }

                    if (i < columnCount) jsonObj.append(", ");
                }

                jsonObj.append("}");
                jsonRows.add(jsonObj.toString());
            }

            String resultsString = "[" + String.join(",\n", jsonRows) + "]";
            logger.info("Result: " + resultsString);
            
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(resultsString);

            resultSet.close();
            statement.close();

            setSuccessMessage("Query executed successfully. JSON Result:\n" + resultsString);

        } catch (Exception e) {
            logger.warn("Error executing HANA query: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Failed to execute HANA query: " + ExceptionUtils.getMessage(e));
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    logger.info("Connection closed.");
                } catch (SQLException e) {
                    logger.warn("Error closing connection: " + e.getMessage());
                }
            }
        }

        return result;
    }
}
