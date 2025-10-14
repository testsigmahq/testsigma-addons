package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;

@Data
@Action(actionText = "Execute DB2 Query on the connection with host host, port port, database dbname, username username, password password",
description = "This action executes given DB2 query against the connection provided and prints the results.",
applicationType = ApplicationType.WEB)
public class Db2Queries extends WebAction {

	@TestData(reference = "Query")
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
	StringBuffer sb = new StringBuffer();

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
			resultSet = stmt.executeQuery(query);
			
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = resultSet.getMetaData().getColumnCount();
			sb.append("Successfully Executed DB2 Query and Resultset is : " + "<br>");
			
			// Print column headers
			for (int i = 1; i <= columnNo; i++) {
				sb.append(rsmd.getColumnName(i));
				if (i < columnNo) sb.append(", ");
			}
			sb.append("<br>");
			
			// Print rows
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
				sb.append("<br>");
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
