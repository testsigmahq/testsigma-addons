package com.testsigma.addons.windowsAdvanced;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Data
@Action(actionText = "Execute DB2 Update Query Query-to-execute on the connection with host host-name, port port-address," +
		" database dbname, username username, password password",
description = "This action executes given DB2 update/insert/delete query against the connection provided " +
		"and prints the no. of affected rows.",
applicationType = ApplicationType.WINDOWS_ADVANCED,
displayName = "Execute DB2 Update Query with parameters",
useCustomScreenshot = true)
public class Db2UpdateQueries extends WindowsAction {

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

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating DB2 Update Query execution");
		
		Connection connection = null;
		Statement stmt = null;

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
			logger.info("Connecting to database...");
			connection = DriverManager.getConnection(url, username, password);
			logger.info("Connection established successfully.");
			stmt = connection.createStatement();
			// Execute update query
			logger.info("Executing update query: " + query);
			int resultdata = stmt.executeUpdate(query);

			sb.append("Successfully Executed DB2 Query and affected rows are : " + resultdata);
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
			// Close resources
			try {
				if (stmt != null) stmt.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				logger.warn("Error closing database resources: " + e.getMessage());
			}
		}
		return result;
	}
}
