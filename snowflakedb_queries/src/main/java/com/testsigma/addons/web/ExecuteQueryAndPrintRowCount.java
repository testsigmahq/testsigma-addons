package com.testsigma.addons.web;


import com.testsigma.addons.util.SnowflakeDBConnection;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

@Data
@Action(actionText = "SnowflakeDB: Execute Query query-string on the Connection db-connection-url",
description = "This action executes given query against the connection provided and prints the no. of affected/fetched rows.",
applicationType = ApplicationType.WEB)
public class ExecuteQueryAndPrintRowCount extends WebAction {

	@TestData(reference = "query-string")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "db-connection-url")
	private com.testsigma.sdk.TestData databaseUrl;
	
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url = databaseUrl.getValue().toString();
		try{
			Statement stmt = SnowflakeDBConnection.getConnection(url);
			String query = testData1.getValue().toString();
			ResultSet resultSet = stmt.executeQuery(query);
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = resultSet.getMetaData().getColumnCount();
			sb.append("Successfully Executed Query and Result-set is : " + "<br>");
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
			result = Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}