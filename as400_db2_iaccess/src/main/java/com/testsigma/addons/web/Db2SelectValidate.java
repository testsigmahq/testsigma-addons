package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Execute DB2 Select_Query on the connection with host host, port port, database dbname, username username, password password and verify output is Expected_Value",
description = "This Action executes a given DB2 Select Query and validates the result(First cell data) against the expected value.",
applicationType = ApplicationType.WEB)
public class Db2SelectValidate extends WebAction {

	@TestData(reference = "Select_Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "host")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "port")
	private com.testsigma.sdk.TestData testData3;
	@TestData(reference = "dbname")
	private com.testsigma.sdk.TestData testData4;
	@TestData(reference = "username")
	private com.testsigma.sdk.TestData testData5;
	@TestData(reference = "password")
	private com.testsigma.sdk.TestData testData6;
	@TestData(reference = "Expected_Value")
	private com.testsigma.sdk.TestData testData7;
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating DB2 Select Query validation execution");
		
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
			resultSet = stmt.executeQuery(query);
			
			if (resultSet.next()) {
				String resultData = resultSet.getObject(1).toString();

				if(testData7.getValue().toString().equals(resultData)) {
					sb.append("<br>The output from the DB2 Select Query is matching with expected value.");
					sb.append("<br>Expected value:" + testData7.getValue().toString());
					sb.append("<br>Actual output from query:" + resultData);
					setSuccessMessage(sb.toString());
					logger.info(sb.toString());
				}
				else {
					result = Result.FAILED;
					sb.append("The selected query value not match with expected value:" + "<br>");
					sb.append("Expected value of select query:" + testData7.getValue().toString() + "<br>");
					sb.append("Actual value from query execution:" + resultData + "<br>");
					setErrorMessage(sb.toString());
					logger.warn(sb.toString());
				}
			} else {
				result = Result.FAILED;
				setErrorMessage("No data returned from the DB2 Select Query");
				logger.warn("No data returned from the DB2 Select Query");
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
			sb.append("<br>" + errorMessage);
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
