package com.testsigma.addons.windowsAdvanced;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Execute DB2 Select-Query Select-Query-to-execute on the connection with host host-name, port port-address," +
		" database dbname, username username, password password and store the complete result set into a runtime " +
		"variable variable-to-store-output",
description = "This Action executes a given DB2 Select Query and stores the complete result set into a provided runtime variable.",
applicationType = ApplicationType.WINDOWS_ADVANCED,
displayName = "Execute DB2 Select Query with parameters and store the result",
useCustomScreenshot = true)
public class Db2Select extends WindowsAction {

	@TestData(reference = "Select-Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "host-name")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "port-address")
	private com.testsigma.sdk.TestData testData3;
	@TestData(reference = "database-name")
	private com.testsigma.sdk.TestData testData4;
	@TestData(reference = "username")
	private com.testsigma.sdk.TestData testData5;
	@TestData(reference = "password")
	private com.testsigma.sdk.TestData testData6;
	@TestData(reference = "variable-to-store-output" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData7;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating DB2 Select Query execution");
		
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
			// url = "jdbc:db2://" + host + ":" + port + "/" + dbName;
			
			logger.info("Connecting to DB2 at: " + url);

			// Load DB2 JDBC Driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");

			// Get connection
			connection = DriverManager.getConnection(url, username, password);
			stmt = connection.createStatement();
			resultSet = stmt.executeQuery(query);
			
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = resultSet.getMetaData().getColumnCount();
			StringBuilder sb = new StringBuilder();
			StringBuilder logBuffer = new StringBuilder();
			// Build complete result string with column headers
			for (int i = 1; i <= columnNo; i++) {
				logBuffer.append(rsmd.getColumnName(i));
				if (i < columnNo) sb.append(", ");
			}

			// Build result string with data rows
			boolean hasData = false;
			while (resultSet.next()) {
				hasData = true;
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
			}
			
			if (hasData) {
				String resultData = sb.toString();
				String columnHeaders = logBuffer.toString();
				runTimeData = new com.testsigma.sdk.RunTimeData();
				runTimeData.setValue(resultData);
				runTimeData.setKey(testData7.getValue().toString());
				setSuccessMessage("Successfully Executed DB2 Select Query and Resultset is : " +columnHeaders +"\n"
						+ resultData);
				logger.info("Successfully Executed DB2 Select Query and Resultset is : " + resultData);
			} else {
				result = Result.FAILED;
				setErrorMessage("No data returned from the DB2 Select Query");
				logger.warn("No data returned from the DB2 Select Query");
			}
		}
		catch (ClassNotFoundException e) {
			String errorMessage = "DB2 Driver not found. Make sure db2jcc4.jar is in classpath. "
					+ ExceptionUtils.getStackTrace(e);
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
