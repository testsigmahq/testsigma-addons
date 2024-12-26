package com.testsigma.addons.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.addons.mysql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Call MySQL Stored Procedure Query on the Connection DB_Connection_URL",
description = "This action executes given query against the connection provided and prints the values",
applicationType = ApplicationType.WEB)
public class MysqlCallProcedure extends WebAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{
			Connection connection = databaseUtil.getConnection(testData2.getValue().toString());
			String query = testData1.getValue().toString();
			CallableStatement callprocedure = connection.prepareCall(query);
			ResultSet resultSet = callprocedure.executeQuery();
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