package com.testsigma.addons.ios;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.Statement;

@Data
@Action(
        actionText = "Create PostgreSQL Procedure Query on the Connection PG_DB_Connection_URL",
        description = "Executes the given PostgreSQL query and creates the procedure",
        applicationType = ApplicationType.IOS
)
public class PostgresCreateProcedure extends IOSAction {

    @TestData(reference = "Query")
    private com.testsigma.sdk.TestData queryData;

    @TestData(reference = "PG_DB_Connection_URL")
    private com.testsigma.sdk.TestData dbUrlData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Starting PostgreSQL procedure creation...");

        try {
            String query = queryData.getValue().toString().trim();
            String dbURL = dbUrlData.getValue().toString().trim();
            logger.info("query: " + query);
            logger.info("dbURL: " + dbURL);

            DatabaseUtil databaseUtil = new DatabaseUtil();

            try (Connection connection = databaseUtil.getConnection(dbURL);
                 Statement statement = connection.createStatement()) {

                statement.execute(query);
                logger.info("PostgreSQL procedure query executed successfully");
            }

            setSuccessMessage("PostgreSQL procedure created successfully.");
            logger.info("PostgreSQL procedure created successfully.");
            return Result.SUCCESS;

        } catch (Exception e) {
            setErrorMessage("Failed to create PostgreSQL procedure: " + ExceptionUtils.getMessage(e));
            logger.warn("PostgreSQL procedure creation failed: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}
