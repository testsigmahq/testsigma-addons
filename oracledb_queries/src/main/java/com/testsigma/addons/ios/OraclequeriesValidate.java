package com.testsigma.addons.ios;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.addons.oracledb.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute OracleDB Query on the Connection DB_Connection_URL and verify affected rows count is Row-Count",
description = "This Action executes given SQL query and validates the affected rows.",
applicationType = ApplicationType.IOS)
public class OraclequeriesValidate extends IOSAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "Row-Count")
	private com.testsigma.sdk.TestData testData3;
	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		StringBuffer sb = new StringBuffer();
		DatabaseUtil databaseUtil = new DatabaseUtil();
		Connection connection = null;
		Statement stmt = null;
		int rowsUpdatedOrFetched = 0;
		try{
			connection = databaseUtil.getConnection(testData2.getValue().toString());
			if (connection == null) {
				result = Result.FAILED;
				setErrorMessage("Failed to establish a database connection. Check your DB_Connection_URL.");
				return result;
			}
			stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			if(query.trim().toUpperCase().startsWith("SELECT")) {
				ResultSet resultSet = stmt.executeQuery(query);
				while (resultSet.next()){
					resultSet.getObject(1).toString();
					rowsUpdatedOrFetched ++;
				}
				sb.append("Successfully Executed Database Query and Rows fetched from DB : " +rowsUpdatedOrFetched + "<br>");
			}else {
				rowsUpdatedOrFetched = stmt.executeUpdate(query);
				sb.append("Successfully Executed Database Query, No. of rows affected in DB : " +rowsUpdatedOrFetched + "<br>");

			}
			if(rowsUpdatedOrFetched == Integer.parseInt(testData3.getValue().toString())) {
				sb.append("Affected row count is matching with expected value." + "<br>");
				setSuccessMessage(sb.toString());
				logger.info(sb.toString());
			}
			else {
				result = com.testsigma.sdk.Result.FAILED;
				sb.append("The affected rows does not match with expected rows:" + "<br>");
				sb.append("Expected no. of affected rows:"+testData3.getValue().toString() + "<br>");
				sb.append("Actual affected rows from query execution:"+rowsUpdatedOrFetched + "<br>");
				setErrorMessage(sb.toString());
				logger.warn(sb.toString());
			}
		}
		catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			sb.append("<br>"+errorMessage);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(sb.toString());
			logger.warn(sb.toString());
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