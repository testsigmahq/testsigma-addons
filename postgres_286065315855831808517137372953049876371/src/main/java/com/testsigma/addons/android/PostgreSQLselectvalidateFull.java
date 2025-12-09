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
@Action(actionText = "Execute PostgresSQL Select_Query on the connection DB_Connection_URL and verify full output" +
		" contains expected json string/ file path Expected_JSON",
description = "This Action executes a given Select Query and validates that all expected data is present in the query output. " +
		"Expected_JSON can be a JSON string or file path. Format: [{\"column1\":\"value1\",\"column2\":\"value2\"},{\"column1\":\"value3\",\"column2\":\"value4\"}]. " +
		"Example for 2 rows: [{\"id\":\"1\",\"name\":\"John\",\"email\":\"john@example.com\"},{\"id\":\"2\",\"name\":\"Jane\",\"email\":\"jane@example.com\"}]. " +
		"The query output can have additional columns/data, but must contain all expected data.",
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
		
		// Check if Expected_JSON is a file path or JSON text
		boolean isFile = databaseUtil.isFilePath(expectedJsonInput);
		if (isFile) {
			logger.info("Expected JSON detected as file path: " + expectedJsonInput);
		} else {
			logger.info("Expected JSON provided as text");
		}

		try (Connection connection = databaseUtil.getConnection(connectionUrl);
			 Statement stmt = connection.createStatement();
			 ResultSet resultSet = stmt.executeQuery(query)) {
			
			// Convert result set to List of Maps using util method
			List<Map<String, Object>> actualRows = databaseUtil.resultSetToList(resultSet);
			
			// Read expected JSON from file or use as text
			String expectedJsonString = databaseUtil.readJsonFromFileOrText(expectedJsonInput);
			
			// Parse expected JSON to List of Maps
			List<Map<String, Object>> expectedRows = databaseUtil.parseJsonToList(expectedJsonString);
			
			// Convert actual to JSON string for display
			String actualJson = databaseUtil.convertToJson(actualRows);
			
			// Check if all expected rows are present in actual results (subset validation)
			boolean allRowsFound = databaseUtil.containsAllRows(expectedRows, actualRows);
			
			if (allRowsFound) {
				StringBuilder message = new StringBuilder();
				message.append("<br>All expected data is present in the query output.");
				message.append("<br>Expected rows: ").append(expectedRows.size());
				message.append("<br>Actual rows: ").append(actualRows.size());
				message.append("<br>Expected JSON:<br><pre>")
				       .append(databaseUtil.maskSensitiveJson(expectedJsonString)).append("</pre>");
				message.append("<br>Actual JSON (first 500 chars):<br><pre>")
				       .append(actualJson.length() > 500 ? 
				       	databaseUtil.maskSensitiveJson(actualJson.substring(0, 500)) + "..." : 
				       	databaseUtil.maskSensitiveJson(actualJson)).append("</pre>");
				setSuccessMessage(message.toString());
				logger.info("JSON validation passed: all expected rows found in query output");
			}
			else {
				result = com.testsigma.sdk.Result.FAILED;
				StringBuilder message = new StringBuilder();
				message.append("Not all expected data is present in the query output:").append("<br>");
				message.append("Expected rows: ").append(expectedRows.size());
				message.append("<br>Actual rows: ").append(actualRows.size());
				message.append("<br>Expected JSON:<br><pre>")
				       .append(databaseUtil.maskSensitiveJson(expectedJsonString)).append("</pre>");
				message.append("<br>Actual JSON (first 500 chars):<br><pre>")
				       .append(actualJson.length() > 500 ? 
				       	databaseUtil.maskSensitiveJson(actualJson.substring(0, 500)) + "..." : 
				       	databaseUtil.maskSensitiveJson(actualJson)).append("</pre>");
				setErrorMessage(message.toString());
				logger.warn("JSON validation failed: not all expected rows found in query output");
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
