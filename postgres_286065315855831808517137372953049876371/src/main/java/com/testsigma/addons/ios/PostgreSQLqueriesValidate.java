package com.testsigma.addons.ios;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.openqa.selenium.NoSuchElementException;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Execute PostgreSQL Query on the Connection DB_Connection_URL and verify affected rows count is Row-Count",
description = "This Action executes given SQL query and validates the affected rows.",
applicationType = ApplicationType.IOS)

public class PostgreSQLqueriesValidate extends IOSAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "Row-Count")
	private com.testsigma.sdk.TestData testData3;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		DatabaseUtil databaseUtil = new DatabaseUtil();
		String connectionUrl = testData2.getValue().toString();
		String query = testData1.getValue().toString();
		int expectedRowCount = Integer.parseInt(testData3.getValue().toString());
		
		logger.info("Initiating execution");
		logger.info("Executing the Query");
		logger.info("Connection URL:" + databaseUtil.maskConnectionUrl(connectionUrl));
		logger.info("Expected Row Count:" + expectedRowCount);

		int rowsUpdatedOrFetched = 0;
		try (Connection connection = databaseUtil.getConnection(connectionUrl);
			 Statement stmt = connection.createStatement()) {
			
			if(query.trim().toUpperCase().startsWith("SELECT")) {
				try (ResultSet resultSet = stmt.executeQuery(query)) {
					while (resultSet.next()){
						resultSet.getObject(1).toString();
						rowsUpdatedOrFetched++;
					}
				}
				StringBuilder sb = new StringBuilder();
				sb.append("Successfully Executed Database Query and Rows fetched from DB : ")
				  .append(rowsUpdatedOrFetched).append("<br>");
				
				if(rowsUpdatedOrFetched == expectedRowCount) {
					sb.append("Affected row count is matching with expected value.").append("<br>");
					setSuccessMessage(sb.toString());
					logger.info("Row count validation passed: " + rowsUpdatedOrFetched);
				} else {
					result = com.testsigma.sdk.Result.FAILED;
					sb.append("The affected rows does not match with expected rows:").append("<br>");
					sb.append("Expected no. of affected rows:").append(expectedRowCount).append("<br>");
					sb.append("Actual affected rows from query execution:").append(rowsUpdatedOrFetched).append("<br>");
					setErrorMessage(sb.toString());
					logger.warn("Row count mismatch. Expected: " + expectedRowCount + ", Actual: " + rowsUpdatedOrFetched);
				}
			} else {
				rowsUpdatedOrFetched = stmt.executeUpdate(query);
				StringBuilder sb = new StringBuilder();
				sb.append("Successfully Executed Database Query, No. of rows affected in DB : ")
				  .append(rowsUpdatedOrFetched).append("<br>");
				
				if(rowsUpdatedOrFetched == expectedRowCount) {
					sb.append("Affected row count is matching with expected value.").append("<br>");
					setSuccessMessage(sb.toString());
					logger.info("Row count validation passed: " + rowsUpdatedOrFetched);
				} else {
					result = com.testsigma.sdk.Result.FAILED;
					sb.append("The affected rows does not match with expected rows:").append("<br>");
					sb.append("Expected no. of affected rows:").append(expectedRowCount).append("<br>");
					sb.append("Actual affected rows from query execution:").append(rowsUpdatedOrFetched).append("<br>");
					setErrorMessage(sb.toString());
					logger.warn("Row count mismatch. Expected: " + expectedRowCount + ", Actual: " + rowsUpdatedOrFetched);
				}
			}
		}
		catch (Exception e){
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Error executing query: " + e.getMessage());
			logger.warn("Error executing query: " + e.getMessage());
		}
		return result;
	}
}
