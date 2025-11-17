package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
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
@Action(actionText = "Execute PostgreSQL Select-Query on the connection DB_Connection_URL and store output into a variable-name",
description = "This Action executes a given Select Query and stores the result(First cell data) into a provided runtime variable.",
applicationType = ApplicationType.WINDOWS_ADVANCED,
useCustomScreenshot = true)

public class PostgreSQLselect extends WindowsAdvancedAction {

	@TestData(reference = "Select-Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "variable-name")
	private com.testsigma.sdk.TestData testData3;
	
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;
	
	@TestStepResult
	private com.testsigma.sdk.TestStepResult testStepResult;
	
	StringBuffer sb = new StringBuffer();

	@Override
	protected Result execute() {
		Result result = Result.SUCCESS;
		logger.info("=== Execute PostgreSQL Select: Starting Execution ===");
		logger.info("Executing the Select Query:"+testData1.getValue().toString());
		logger.info("Connection URL:"+testData2.getValue().toString());
		logger.info("Variable Name:"+testData3.getValue().toString());

		logger.info("Initiating execution");
		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{
			Connection connection = databaseUtil.getConnection(testData2.getValue().toString());
			Statement stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			ResultSet resultSet = stmt.executeQuery(query);
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = resultSet.getMetaData().getColumnCount();
			for (int i = 1; i <= columnNo; i++) {
		        	}
			while (resultSet.next()) {
				 for (int j = 1; j <= columnNo; j++) {
			           if (j > 1) sb.append(", ");
			           String columnValue = resultSet.getString(j);
			           if (resultSet.wasNull()) {
			        	   sb.append("");
			        	}
			           sb.append(columnValue);
				 }
			}
			runTimeData = new com.testsigma.sdk.RunTimeData();
			runTimeData.setValue(sb.toString());
			runTimeData.setKey(testData3.getValue().toString());
			setSuccessMessage("Successfully Executed Select Query and Resultset is : " +sb.toString());
			logger.info("Successfully Executed Select Query and Resultset is : " +sb.toString());
			ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
					"postgresql_select_screenshot", logger);
		}
		catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
			ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
					"postgresql_select_failure_screenshot", logger);
		}
		return result;
	}
}

