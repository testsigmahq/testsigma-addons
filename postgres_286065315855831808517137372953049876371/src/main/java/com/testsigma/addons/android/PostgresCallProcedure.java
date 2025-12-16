package com.testsigma.addons.android;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.CallableStatement;
import java.sql.Connection;

@Data
@Action(
        actionText = "Call PostgreSQL Stored Procedure Query on the Connection PG_DB_Connection_URL",
        description = "Executes a PostgreSQL CALL procedure query",
        applicationType = ApplicationType.ANDROID
)
public class PostgresCallProcedure extends AndroidAction {

    @TestData(reference = "Query")
    private com.testsigma.sdk.TestData procedureQueryData;

    @TestData(reference = "PG_DB_Connection_URL")
    private com.testsigma.sdk.TestData dbUrlData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Starting PostgreSQL procedure execution...");

        try {
            String callQuery = procedureQueryData.getValue().toString().trim();
            String dbURL = dbUrlData.getValue().toString().trim();
            logger.info("Database URL: " + dbURL);
            logger.info("Call Query: " + callQuery);

            DatabaseUtil databaseUtil = new DatabaseUtil();

            try (Connection connection = databaseUtil.getConnection(dbURL);
                 CallableStatement callableStatement = connection.prepareCall(callQuery)) {

                callableStatement.execute();
                logger.info("Successfully executed Procedure Query");
            }

            setSuccessMessage("PostgreSQL procedure executed successfully.");
            logger.info("PostgreSQL procedure executed successfully.");
            return Result.SUCCESS;

        } catch (Exception e) {
            setErrorMessage("Failed to execute PostgreSQL procedure: " + ExceptionUtils.getMessage(e));
            logger.warn("PostgreSQL procedure execution failed: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}
