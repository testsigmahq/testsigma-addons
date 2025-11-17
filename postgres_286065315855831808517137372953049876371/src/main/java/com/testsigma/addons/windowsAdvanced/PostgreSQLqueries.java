package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Execute PostgreSQL Query on the Connection DB_Connection_URL",
description = "This action executes given query against the connection provided and prints the no. of affected/fetched rows.",
applicationType = ApplicationType.WINDOWS_ADVANCED,
useCustomScreenshot = true)

public class PostgreSQLqueries extends WindowsAdvancedAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	
	@TestStepResult
	private com.testsigma.sdk.TestStepResult testStepResult;
	
	StringBuffer sb = new StringBuffer();

	@Override
	protected Result execute() {
		Result result = Result.SUCCESS;
		logger.info("=== Execute PostgreSQL Query: Starting Execution ===");
		logger.info("Executing the Query:"+testData1.getValue().toString());
		logger.info("Connection URL:"+testData2.getValue().toString());

		logger.info("Initiating execution");
		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{
			Connection connection = databaseUtil.getConnection(testData2.getValue().toString());
			Statement stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			ResultSet resultSet = stmt.executeQuery(query);
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = resultSet.getMetaData().getColumnCount();
			sb.append("Successfully Executed Query and Resultset is : " + "<br>");
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
				 }
				 sb.append("<br>");
			}
			setSuccessMessage(sb.toString());
			logger.info(sb.toString());
		}
		catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}

