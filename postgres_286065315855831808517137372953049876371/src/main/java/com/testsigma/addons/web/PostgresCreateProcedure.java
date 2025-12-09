package com.testsigma.addons.web;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.Connection;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(
        actionText = "Create Procedure Query on the Connection PG_DB_Connection_URL",
        description = "Executes the given PostgreSQL query and creates the procedure",
        applicationType = ApplicationType.WEB
)
public class PostgresCreateProcedure extends WebAction {

    @TestData(reference = "Query")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "PG_DB_Connection_URL")
    private com.testsigma.sdk.TestData testData2;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Starting PostgreSQL procedure creation...");

        DatabaseUtil databaseUtil = new DatabaseUtil();

        try {
            String query = testData1.getValue().toString();
            String dbURL = testData2.getValue().toString();

            logger.info("Connecting to PostgreSQL DB...");
            Connection connection = databaseUtil.getConnection(dbURL);

            Statement stmt = connection.createStatement();
            stmt.execute(query);

            setSuccessMessage("PostgreSQL procedure created successfully: " + query);
            logger.info("PostgreSQL procedure created successfully: " + query);

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
