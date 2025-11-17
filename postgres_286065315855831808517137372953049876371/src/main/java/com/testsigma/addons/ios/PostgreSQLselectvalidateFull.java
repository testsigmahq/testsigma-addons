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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Execute PostgreSQL Select_Query on the connection DB_Connection_URL and verify full output matches Expected_JSON",
description = "This Action executes a given Select Query and validates the entire result set (with column names and all rows) against the expected JSON format. " +
		"Expected_JSON should be in format: [{\"column1\":\"value1\",\"column2\":\"value2\"},{\"column1\":\"value3\",\"column2\":\"value4\"}]. " +
		"Example for 2 rows: [{\"id\":\"1\",\"name\":\"John\",\"email\":\"john@example.com\"},{\"id\":\"2\",\"name\":\"Jane\",\"email\":\"jane@example.com\"}]",
applicationType = ApplicationType.IOS)

public class PostgreSQLselectvalidateFull extends IOSAction {

	@TestData(reference = "Select_Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "Expected_JSON")
	private com.testsigma.sdk.TestData testData3;
	
	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		logger.info("Executing the Select Query:"+testData1.getValue().toString());
		logger.info("Connection URL:"+testData2.getValue().toString());
		logger.info("Expected JSON:"+testData3.getValue().toString());

		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{
			Connection connection = databaseUtil.getConnection(testData2.getValue().toString());
			Statement stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			ResultSet resultSet = stmt.executeQuery(query);
			
			// Convert result set to List of Maps using util method
			List<Map<String, Object>> resultRows = databaseUtil.resultSetToList(resultSet);
			
			// Convert to JSON string using util method
			String actualJson = databaseUtil.convertToJson(resultRows);
			
			// Normalize expected JSON (remove whitespace for comparison)
			String expectedJson = databaseUtil.normalizeJson(testData3.getValue().toString());
			String normalizedActualJson = databaseUtil.normalizeJson(actualJson);
			
			// Compare JSON strings
			if(expectedJson.equals(normalizedActualJson)) {
				sb.append("<br>The output from the Select Query matches the expected JSON.");
				sb.append("<br>Expected JSON:<br><pre>").append(testData3.getValue().toString()).append("</pre>");
				sb.append("<br>Actual JSON:<br><pre>").append(actualJson).append("</pre>");
				setSuccessMessage(sb.toString());
				logger.info(sb.toString());
			}
			else {
				result = com.testsigma.sdk.Result.FAILED;
				sb.append("The query output does not match the expected JSON:" + "<br>");
				sb.append("Expected JSON:<br><pre>").append(testData3.getValue().toString()).append("</pre>");
				sb.append("<br>Actual JSON:<br><pre>").append(actualJson).append("</pre>");
				setErrorMessage(sb.toString());
				logger.warn(sb.toString());
			}
		}
		catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			sb.append("<br>").append(errorMessage);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(sb.toString());
			logger.warn(sb.toString());
		}
		return result;
	}
}

