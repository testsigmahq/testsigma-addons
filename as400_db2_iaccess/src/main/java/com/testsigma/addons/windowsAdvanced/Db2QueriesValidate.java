package com.testsigma.addons.windowsAdvanced;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;

@Data
@Action(actionText = "Execute DB2 Query Query-to-execute on the connection with host host-name, port port-address," +
		" database dbname, username username, password password and verify affected rows count is Row-Count",
description = "This Action executes given DB2 SQL query and validates the affected rows.",
applicationType = ApplicationType.WINDOWS_ADVANCED,
displayName = "Execute DB2 Query with validation",
useCustomScreenshot = true)
public class Db2QueriesValidate extends WindowsAction {

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
	@TestData(reference = "Row-Count")
	private com.testsigma.sdk.TestData testData7;
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
			
			if(query.trim().toUpperCase().startsWith("SELECT")) {
				resultSet = stmt.executeQuery(query);
				while (resultSet.next()){
					resultSet.getObject(1).toString();
					rowsUpdatedOrFetched++;
				}
				sb.append("Successfully Executed DB2 Query and Rows fetched from DB : " + rowsUpdatedOrFetched + "\n");
			}else {
				rowsUpdatedOrFetched = stmt.executeUpdate(query);
				sb.append("Successfully Executed DB2 Query, No. of rows affected in DB : " + rowsUpdatedOrFetched + "\n");
			}
			
			if(rowsUpdatedOrFetched == Integer.parseInt(testData7.getValue().toString())) {
				sb.append("Affected row count is matching with expected value." + "\n");
				setSuccessMessage(sb.toString());
				logger.info(sb.toString());
			}
			else {
				result = Result.FAILED;
				sb.append("The affected rows does not match with expected rows:" + "\n");
				sb.append("Expected no. of affected rows:" + testData7.getValue().toString() + "\n");
				sb.append("Actual affected rows from query execution:" + rowsUpdatedOrFetched + "\n");
				setErrorMessage(sb.toString());
				logger.warn(sb.toString());
			}
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
