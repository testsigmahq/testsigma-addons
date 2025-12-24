package com.testsigma.addons.mobileweb;

import com.testsigma.addons.oracledb.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute OracleDB Select-Query on the connection DB_Connection_URL and store output into a variable-name",
description = "This Action executes a given Select Query and stores the result(First cell data) into a provided runtime variable.",
applicationType = ApplicationType.MOBILE_WEB)
public class Oracleselect extends WebAction {

	@TestData(reference = "Select-Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "variable-name", isRuntimeVariable=true)
	private com.testsigma.sdk.TestData testData3;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;
	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		DatabaseUtil databaseUtil = new DatabaseUtil();
		Connection connection = null;
		Statement stmt = null;
		try{
			connection = databaseUtil.getConnection(testData2.getValue().toString());
			if (connection == null) {
				result = Result.FAILED;
				setErrorMessage("Failed to establish a database connection. Check your DB_Connection_URL.");
				return result;
			}
			stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			ResultSet resultSet = stmt.executeQuery(query);
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = resultSet.getMetaData().getColumnCount();
			sb.append("<br>");
			for (int i = 1; i <= columnNo; i++) {
		           sb.append(rsmd.getColumnName(i));
		           sb.append(", ");
		        	}
			   sb.append("<br>");
			while (resultSet.next()) {
				 for (int j = 1; j <= columnNo; j++) {
			           if (j > 1) sb.append(", ");
			           String columnValue = resultSet.getString(j);
			           if (resultSet.wasNull()) {
			        	   sb.append("");
			        	}
			           sb.append(columnValue);
			           runTimeData = new com.testsigma.sdk.RunTimeData();
					   runTimeData.setValue(columnValue);
					   runTimeData.setKey(testData3.getValue().toString());
				 }
				 sb.append("<br>");
			}
			setSuccessMessage("Successfully Executed Select Query and Resultset is : " +sb.toString());
			logger.info("Successfully Executed Select Query and Resultset is : " +sb.toString());
		}
		catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.warn("Error closing statement: " + e.getMessage() + e);
			}

			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.warn("Error closing connection: " + e.getMessage() + e);
			}
		}
		return result;
	}
}