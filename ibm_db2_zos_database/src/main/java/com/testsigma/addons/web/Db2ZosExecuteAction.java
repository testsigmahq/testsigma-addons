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
import java.sql.SQLException;
import java.sql.Statement;

@Data
@Action(actionText = "DB2 ZOS: Execute insert-update-delete-query using jdbc-url jdbc-url, username username and password password and store the number of affected rows in rows-affected",
        description = "Connects to DB2 for z/OS via the IBM Data Server Driver (JCC) and executes any INSERT/UPDATE/DELETE/DDL statement, storing the number of affected rows in a runtime variable.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class Db2ZosExecuteAction extends WebAction {

    @TestData(reference = "insert-update-delete-query")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "jdbc-url")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "username")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "password")
    private com.testsigma.sdk.TestData testData4;
    @TestData(reference = "rows-affected", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData5;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating execution com.testsigma.addons.web.Db2ZosExecuteAction");
        Connection connection = null;
        Statement stmt = null;
        try {
            String sql = testData1.getValue().toString();
            String jdbcUrl = testData2.getValue().toString();
            String username = testData3.getValue().toString();
            String password = testData4.getValue().toString();

            logger.info("Executing: " + sql);

            connection = Db2Util.connect(jdbcUrl, username, password, logger);
            stmt = connection.createStatement();
            int rowsAffected = stmt.executeUpdate(sql);

            runTimeData.setKey(testData5.getValue().toString());
            runTimeData.setValue(String.valueOf(rowsAffected));

            setSuccessMessage("Statement affected " + rowsAffected + " row(s), stored in "
                    + testData5.getValue() + ": " + rowsAffected);
            return Result.SUCCESS;
        } catch (ClassNotFoundException e) {
            logger.warn("DB2 JCC driver not found on classpath: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("DB2 driver not found: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        } catch (SQLException e) {
            logger.warn("SQL error while executing DB2 z/OS statement: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("SQL error: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        } catch (Exception e) {
            logger.warn("Exception occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error occurred while executing DB2 z/OS statement: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        } finally {
            Db2Util.close(connection, stmt, null, logger);
        }
    }
}
