package com.testsigma.addons.mobileweb;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute PostgreSQL Select_Query on the connection DB_Connection_URL and verify output is Expected_Value",
description = "This Action executes a given Select Query and validates the result(First cell data) aginst the expected value.",
applicationType = ApplicationType.MOBILE_WEB)

public class PostgreSQLselectvalidate extends WebAction {

	@TestData(reference = "Select_Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "Expected_Value")
	private com.testsigma.sdk.TestData testData3;
	
	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Executing the Select Query:"+testData1.getValue().toString());
		logger.info("Connection URL:"+testData2.getValue().toString());
		logger.info("Expected Value:"+testData3.getValue().toString());

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

			if(testData3.getValue().toString().equals(sb.toString())) {
				sb.append("<br>The output from the Select Query is matching with expected value.");
				sb.append("<br>Expected value:"+testData3.getValue().toString());
				sb.append("<br>Actual output from query:"+sb.toString());
				setSuccessMessage(sb.toString());
				logger.info(sb.toString());
			}
			else {
				result = com.testsigma.sdk.Result.FAILED;
				sb.append("The selected query value not match with expected rows:" + "<br>");
				sb.append("Expected value of select query:"+testData3.getValue().toString() + "<br>");
				sb.append("Actual value from query execution:"+sb.toString() + "<br>");
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
		}
		return result;
	}
}