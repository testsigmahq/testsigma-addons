package com.testsigma.addons.android;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.NoSuchElementException;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Execute PostgreSQL Select_Query on the connection DB_Connection_URL and verify full output matches Expected_JSON",
description = "This Action executes a given Select Query and validates the entire result set (with column names and all rows) against the expected JSON format. " +
		"Expected_JSON should be in format: [{\"column1\":\"value1\",\"column2\":\"value2\"},{\"column1\":\"value3\",\"column2\":\"value4\"}]. " +
		"Example for 2 rows: [{\"id\":\"1\",\"name\":\"John\",\"email\":\"john@example.com\"},{\"id\":\"2\",\"name\":\"Jane\",\"email\":\"jane@example.com\"}]",
applicationType = ApplicationType.ANDROID)

public class PostgreSQLselectvalidateFull extends AndroidAction {

	@TestData(reference = "Select_Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "Expected_JSON")
	private com.testsigma.sdk.TestData testData3;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		DatabaseUtil databaseUtil = new DatabaseUtil();
		String connectionUrl = testData2.getValue().toString();
		String query = testData1.getValue().toString();
		String expectedJsonInput = testData3.getValue().toString();
		
		logger.info("Initiating execution");
		logger.info("Executing the Select Query");
		logger.info("Connection URL:" + databaseUtil.maskConnectionUrl(connectionUrl));
		logger.info("Expected JSON provided");

		try (Connection connection = databaseUtil.getConnection(connectionUrl);
			 Statement stmt = connection.createStatement();
			 ResultSet resultSet = stmt.executeQuery(query)) {
			
			// Convert result set to List of Maps using util method
			List<Map<String, Object>> resultRows = databaseUtil.resultSetToList(resultSet);
			
			// Convert to JSON string using util method
			String actualJson = databaseUtil.convertToJson(resultRows);
			
			// Normalize expected JSON (remove whitespace for comparison)
			String expectedJson = databaseUtil.normalizeJson(expectedJsonInput);
			String normalizedActualJson = databaseUtil.normalizeJson(actualJson);
			
			// Compare JSON strings
			if(expectedJson.equals(normalizedActualJson)) {
				StringBuilder message = new StringBuilder();
				message.append("<br>The output from the Select Query matches the expected JSON.");
				message.append("<br>Expected JSON:<br><pre>")
				       .append(databaseUtil.maskSensitiveJson(expectedJsonInput)).append("</pre>");
				message.append("<br>Actual JSON:<br><pre>")
				       .append(databaseUtil.maskSensitiveJson(actualJson)).append("</pre>");
				setSuccessMessage(message.toString());
				logger.info("JSON validation passed: output matches expected JSON");
			}
			else {
				result = com.testsigma.sdk.Result.FAILED;
				StringBuilder message = new StringBuilder();
				message.append("The query output does not match the expected JSON:").append("<br>");
				message.append("Expected JSON:<br><pre>")
				       .append(databaseUtil.maskSensitiveJson(expectedJsonInput)).append("</pre>");
				message.append("<br>Actual JSON:<br><pre>")
				       .append(databaseUtil.maskSensitiveJson(actualJson)).append("</pre>");
				setErrorMessage(message.toString());
				logger.warn("JSON validation failed: output does not match expected JSON");
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
