package com.testsigma.addons.web;

import com.testsigma.addons.util.Db2Util;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Data
@Action(actionText = "DB2 ZOS: Execute select-query on the connection with jdbc-url jdbc-url, username username, password password and store the result in result-variable",
        description = "Connects to DB2 for z/OS via the IBM Data Server Driver (JCC) using a jdbc:db2: URL, executes a SELECT query, and stores the result rows in a runtime variable.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class Db2ZosQueryAction extends WebAction {

    @TestData(reference = "select-query")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "jdbc-url")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "username")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "password")
    private com.testsigma.sdk.TestData testData4;
    @TestData(reference = "result-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData5;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating execution com.testsigma.addons.web.Db2ZosQueryAction");
        Connection connection = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            String query = testData1.getValue().toString();
            String jdbcUrl = testData2.getValue().toString();
            String username = testData3.getValue().toString();
            String password = testData4.getValue().toString();

            connection = Db2Util.connect(jdbcUrl, username, password, logger);
            stmt = connection.createStatement();

            resultSet = stmt.executeQuery(query);
            String resultText = Db2Util.formatResultSet(resultSet);
            int rowCount = resultText.isEmpty() ? 0 : resultText.split(System.lineSeparator()).length;

            runTimeData.setKey(testData5.getValue().toString());
            runTimeData.setValue(resultText);

            setSuccessMessage("Query returned " + rowCount + " row(s), stored in "
                    + testData5.getValue() + ": " + resultText);
            return Result.SUCCESS;
        } catch (ClassNotFoundException e) {
            logger.warn("DB2 JCC driver not found on classpath: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("DB2 driver not found: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        } catch (SQLException e) {
            logger.warn("SQL error while executing DB2 z/OS query: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("SQL error: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        } catch (Exception e) {
            logger.warn("Exception occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error occurred while executing DB2 z/OS query: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        } finally {
            Db2Util.close(connection, stmt, resultSet, logger);
        }
    }
}
