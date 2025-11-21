package com.testsigma.addons.web;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.openqa.selenium.NoSuchElementException;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Execute PostgreSQL Select_Query on the connection DB_Connection_URL and verify output is Expected_Value",
description = "This Action executes a given Select Query and validates the result(First cell data) aginst the expected value.",
applicationType = ApplicationType.WEB)

public class PostgreSQLselectvalidate extends WebAction {

	@TestData(reference = "Select_Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "Expected_Value")
	private com.testsigma.sdk.TestData testData3;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		DatabaseUtil databaseUtil = new DatabaseUtil();
		String connectionUrl = testData2.getValue().toString();
		String query = testData1.getValue().toString();
		String expectedValue = testData3.getValue().toString();
		
		logger.info("Initiating execution");
		logger.info("Executing the Select Query");
		logger.info("Connection URL:" + databaseUtil.maskConnectionUrl(connectionUrl));
		logger.info("Expected Value provided");

		try (Connection connection = databaseUtil.getConnection(connectionUrl);
			Statement stmt = connection.createStatement();
			 ResultSet resultSet = stmt.executeQuery(query)) {
			
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = rsmd.getColumnCount();
			StringBuilder sb = new StringBuilder();
			
			while (resultSet.next()) {
				 for (int j = 1; j <= columnNo; j++) {
			           if (j > 1) sb.append(", ");
			           String columnValue = resultSet.getString(j);
			           if (resultSet.wasNull()) {
			        	   sb.append("");
					} else {
			           sb.append(columnValue);
					}
				}
			}
			
			String actualValue = sb.toString();
			if(expectedValue.equals(actualValue)) {
				StringBuilder message = new StringBuilder();
				message.append("<br>The output from the Select Query is matching with expected value.");
				message.append("<br>Expected value:").append(expectedValue);
				message.append("<br>Actual output from query:").append(actualValue);
				setSuccessMessage(message.toString());
				logger.info("Validation passed: output matches expected value");
			}
			else {
				result = com.testsigma.sdk.Result.FAILED;
				StringBuilder message = new StringBuilder();
				message.append("The selected query value not match with expected rows:").append("<br>");
				message.append("Expected value of select query:").append(expectedValue).append("<br>");
				message.append("Actual value from query execution:").append(actualValue).append("<br>");
				setErrorMessage(message.toString());
				logger.warn("Validation failed: output does not match expected value");
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