package com.testsigma.addons.mobileweb;



import com.testsigma.addons.oracledb.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Data
@Action(actionText = "Execute OracleDB Update or Delete Query on DB_Connection_URL and store affected rows in runtime variable variable-name",
        description = "This action executes an update or delete query against the provided OracleDB connection. It stores the number of affected rows in the specified runtime variable.",
        applicationType = ApplicationType.MOBILE_WEB)
public class OracleUpdateDeletequeries extends WebAction {

    @TestData(reference = "Query")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "DB_Connection_URL")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    private Integer affectedRows;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        DatabaseUtil databaseUtil = new DatabaseUtil();
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = databaseUtil.getConnection(testData2.getValue().toString());
            if (connection == null) {
                result = Result.FAILED;
                setErrorMessage("Failed to establish a database connection. Check your DB_Connection_URL.");
                return result;
            }

            stmt = connection.createStatement();
            String query = testData1.getValue().toString();
            affectedRows = stmt.executeUpdate(query);
            String message = "Successfully executed the query. Number of rows affected: " + affectedRows;
            setSuccessMessage(message);
            logger.info(message);

            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(affectedRows.toString());
            setSuccessMessage(getSuccessMessage() + " Result stored in runtime variable '" + variableName.getValue().toString() + "'");
            logger.info("Result stored in runtime variable '" + variableName.getValue().toString() + "'");


        } catch (SQLException e) {
            String errorMessage = "SQL Exception occurred: " + e.getMessage() + ". Check your query syntax and database connection.";
            result = Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage + e);
        } catch (Exception e) {
            String errorMessage = "An unexpected error occurred: " + e.getMessage();
            result = Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage + e);
        }

        return result;
    }
}