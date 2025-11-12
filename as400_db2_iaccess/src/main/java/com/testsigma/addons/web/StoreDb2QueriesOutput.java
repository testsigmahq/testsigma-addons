package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;

@Data
@Action(actionText = "Execute DB2 Query Query-to-execute on the connection with host host-name, port port-address," +
		" database dbname, username username, password password and store the result in runtime variable variable_name",
description = "This Action executes given DB2 SQL query and stores the output in runtime variable.",
applicationType = ApplicationType.WEB)
public class StoreDb2QueriesOutput extends WebAction {

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
	@TestData(reference = "variable_name", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData7;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runtimeData;

	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating DB2 Query validation execution");

		Connection connection = null;
		Statement stmt = null;
		ResultSet resultSet = null;

		int rowsUpdatedOrFetched = 0;
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

		// Execute query and store output
		String queryOutput = "";
		runtimeData.setKey(testData7.getValue().toString());

		if(query.trim().toUpperCase().startsWith("SELECT")) {
			// Handle SELECT queries - store actual result data
			resultSet = stmt.executeQuery(query);
			StringBuilder resultBuilder = new StringBuilder();
			
			// Get column metadata
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			
			// Build header row
			for (int i = 1; i <= columnCount; i++) {
				if (i > 1) resultBuilder.append(" | ");
				resultBuilder.append(metaData.getColumnName(i));
			}
			resultBuilder.append("\n");
			
			// Add separator line
			for (int i = 1; i <= columnCount; i++) {
				if (i > 1) resultBuilder.append("-+-");
				resultBuilder.append("---");
			}
			resultBuilder.append("\n");
			
			// Process result rows
			while (resultSet.next()) {
				for (int i = 1; i <= columnCount; i++) {
					if (i > 1) resultBuilder.append(" | ");
					Object value = resultSet.getObject(i);
					resultBuilder.append(value != null ? value.toString() : "NULL");
				}
				resultBuilder.append("\n");
				rowsUpdatedOrFetched++;
			}
			
			queryOutput = resultBuilder.toString();
			sb.append("Successfully Executed SELECT Query and fetched " + rowsUpdatedOrFetched + " rows from DB\n");
		} else {
			// Handle non-SELECT queries (INSERT, UPDATE, DELETE, etc.)
			rowsUpdatedOrFetched = stmt.executeUpdate(query);
			queryOutput = "Query executed successfully. Rows affected: " + rowsUpdatedOrFetched;
			sb.append("Successfully Executed DB2 Query, No. of rows affected in DB : " + rowsUpdatedOrFetched + "\n");
		}

		// Store the output in runtime variable
		runtimeData.setValue(queryOutput);
		
		// Set success message
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
			sb.append("\n" + errorMessage);
			result = Result.FAILED;
			setErrorMessage(sb.toString());
			logger.warn(sb.toString());
		}
		finally {
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
