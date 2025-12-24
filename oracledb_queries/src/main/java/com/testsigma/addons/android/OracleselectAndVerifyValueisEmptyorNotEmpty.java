package com.testsigma.addons.android;

import com.testsigma.addons.oracledb.util.DatabaseUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Data
@Action(
        actionText = "Execute OracleDB Select-Query on the connection DB_Connection_URL and verify result is options",
        description = "This action executes a SELECT query and verifies whether the result is EMPTY or NOT EMPTY.",
        applicationType = ApplicationType.ANDROID
)
public class OracleselectAndVerifyValueisEmptyorNotEmpty extends AndroidAction {

    @TestData(reference = "Select-Query")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "DB_Connection_URL")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "options", allowedValues = {"EMPTY", "NOT EMPTY"})
    private com.testsigma.sdk.TestData testData3;

    @Override
    public Result execute() throws NoSuchElementException {

        Result result = Result.SUCCESS;
        logger.info("Initiating Oracle SELECT query execution");

        DatabaseUtil databaseUtil = new DatabaseUtil();
        Connection connection = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            String query = testData1.getValue().toString();
            String dbUrl = testData2.getValue().toString();
            String optionsVal = testData3.getValue().toString().toLowerCase();

            logger.info("Query: " + query);
            logger.info("DB Connection URL: " + dbUrl);
            logger.info("Validation Option: " + optionsVal);

            connection = databaseUtil.getConnection(dbUrl);
            stmt = connection.createStatement();
            resultSet = stmt.executeQuery(query);

            switch (optionsVal) {

                case "empty":
                    if (resultSet.next()) {
                        result = Result.FAILED;
                        setErrorMessage(
                                "Query executed successfully but returned records."
                        );
                        logger.warn("ResultSet is NOT empty");
                    } else {
                        setSuccessMessage(
                                "Query executed successfully and result is EMPTY."
                        );
                        logger.info("ResultSet is empty");
                    }
                    break;

                case "not empty":
                    if (resultSet.next()) {
                        setSuccessMessage(
                                "Query executed successfully and result is NOT EMPTY."
                        );
                        logger.info("ResultSet is NOT empty");
                    } else {
                        result = Result.FAILED;
                        setErrorMessage(
                                "Query executed successfully but returned NO records."
                        );
                        logger.warn("ResultSet is empty");
                    }
                    break;

                default:
                    result = Result.FAILED;
                    setErrorMessage(
                            "Invalid option provided. Allowed values are EMPTY or NOT EMPTY."
                    );
                    logger.warn("Invalid validation option received");
            }

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("Exception Occurred: " + ExceptionUtils.getMessage(e));
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));

        } finally {

            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                logger.warn("Error closing ResultSet: " + ExceptionUtils.getStackTrace(e));
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.warn("Error closing Statement: " + ExceptionUtils.getStackTrace(e));
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.warn("Error closing Connection: " + ExceptionUtils.getStackTrace(e));
            }
        }

        return result;
    }
}
