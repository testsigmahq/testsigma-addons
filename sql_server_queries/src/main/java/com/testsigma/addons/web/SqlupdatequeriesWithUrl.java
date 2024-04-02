package com.testsigma.addons.web;

import com.testsigma.addons.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.Statement;

@Data
@Action(actionText = "SQLServer: Execute SQL Update Query on the Connection DB_Connection_URL",
description = "This action executes given update query against the connection provided and prints the no. of affected/fetched rows.",
applicationType = ApplicationType.WEB)
public class SqlupdatequeriesWithUrl extends WebAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData databaseUrl;
	
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url = databaseUrl.getValue().toString();
		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{
			Connection connection = databaseUtil.getConnection(url);
			Statement stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			int resultdata = stmt.executeUpdate(query);

			sb.append("Successfully Executed Query and affected rows are : " + "<br>" +resultdata);
			
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