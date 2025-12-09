package com.testsigma.addons.web;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Data
@Action(actionText = "Call PostgreSQL Stored Procedure Query on the Connection PG_DB_Connection_URL", description = "Executes a PostgreSQL CALL query", applicationType = ApplicationType.WEB)
public class PostgresCallProcedure extends WebAction {

    @TestData(reference = "Query")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "PG_DB_Connection_URL")
    private com.testsigma.sdk.TestData testData2;

    StringBuffer sb = new StringBuffer();

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Executing PostgreSQL CALL....");

        DatabaseUtil databaseUtil = new DatabaseUtil();

        try {

            String query = testData1.getValue().toString().trim();
            String dbURL = testData2.getValue().toString();

            Connection connection = databaseUtil.getConnection(dbURL);

            PreparedStatement stmt = connection.prepareStatement(query);

            boolean hasResultSet = stmt.execute();

            setSuccessMessage("Query executed successfully");
            logger.info(sb.toString());
            stmt.close();
            connection.close();

        } catch (Exception e) {
            setErrorMessage("Exception Occurred: " + ExceptionUtils.getMessage(e));
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }

        return result;
    }
}
