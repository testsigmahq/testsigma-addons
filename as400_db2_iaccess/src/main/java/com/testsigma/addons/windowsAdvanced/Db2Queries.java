package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;

@Data
@Action(actionText = "Execute DB2 query Query-to-execute on the connection with host host-name, port port-address," +
		" database dbname, username username, password password",
description = "This action executes any type of DB2 query (SELECT, INSERT, UPDATE, DELETE, etc.) and displays the results.",
applicationType = ApplicationType.WINDOWS_ADVANCED,
displayName = "Execute any DB2 Query",
useCustomScreenshot = true)
public class Db2Queries extends WindowsAction {

	@TestData(reference = "Query-to-execute")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "host-name")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "port-address")
	private com.testsigma.sdk.TestData testData3;
	@TestData(reference = "dbname")
	private com.testsigma.sdk.TestData testData4;
	@TestData(reference = "username")
	private com.testsigma.sdk.TestData testData5;
	@TestData(reference = "password")
	private com.testsigma.sdk.TestData testData6;
	StringBuffer sb = new StringBuffer();
	StringBuffer logBuffer = new StringBuffer();
	@TestStepResult
	private com.testsigma.sdk.TestStepResult testStepResult;

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating DB2 Query execution");
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet resultSet = null;

		try{
			// Get connection parameters
			String host = testData2.getValue().toString();
			String port = testData3.getValue().toString();
			String dbName = testData4.getValue().toString();
			String username = testData5.getValue().toString();
			String password = testData6.getValue().toString();
			String query = testData1.getValue().toString();

			// Build DB2 JDBC URL
			String url = "jdbc:as400://" + host + ":" + port + "/" + dbName;
			
			logger.info("Connecting to DB2 at: " + url);

			// Load DB2 JDBC Driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");

		// Get connection
		connection = DriverManager.getConnection(url, username, password);
		stmt = connection.createStatement();
		
		// Check query type and execute accordingly
		if(query.trim().toUpperCase().startsWith("SELECT")) {
			// Handle SELECT queries - display result set
			resultSet = stmt.executeQuery(query);
			
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = resultSet.getMetaData().getColumnCount();
			sb.append("Successfully Executed SELECT Query and Resultset is : " + "\n");
			
			// Print column headers
			for (int i = 1; i <= columnNo; i++) {
				sb.append(rsmd.getColumnName(i));
				if (i < columnNo) sb.append(", ");
			}
			sb.append("\n");
			
			// Print rows
			int rowCount = 0;
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
				sb.append("\n");
				rowCount++;
			}
			sb.append("\nTotal rows fetched: " + rowCount + "\n");
		} else {
			// Handle non-SELECT queries (INSERT, UPDATE, DELETE, etc.)
			int rowsAffected = stmt.executeUpdate(query);
			sb.append("Successfully Executed DB2 Query: " + query.trim().toUpperCase().split("\\s+")[0] + "\n");
			sb.append("Number of rows affected: " + rowsAffected + "\n");
		}
		
		setSuccessMessage(sb.toString());
		logger.info(sb.toString());
		}
		catch (ClassNotFoundException e) {
			String errorMessage = "DB2 Driver not found. Make sure db2jcc4.jar is in classpath. " + ExceptionUtils.getStackTrace(e);
			result = Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		catch (SQLException e) {
			String errorMessage = "SQL Error: " + e.getMessage() + " " + ExceptionUtils.getStackTrace(e);
			result = Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		finally {
			// Capture and upload screenshot
			ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "db2_query_execution", logger);
			// Close resources
			try {
				if (resultSet != null) resultSet.close();
				if (stmt != null) stmt.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				logger.warn("Error closing database resources: " + e.getMessage());
			}
		}
		return result;
	}
}
