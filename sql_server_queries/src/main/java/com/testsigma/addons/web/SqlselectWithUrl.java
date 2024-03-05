package com.testsigma.addons.web;

import com.testsigma.addons.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

@Data
@Action(actionText = "SQLServer: Execute SQL Select-Query on the connection DB_Connection_URL and store output into a variable-name",
description = "This Action executes a given Select Query and stores the result(First cell data) into a provided runtime variable.",
applicationType = ApplicationType.WEB)
public class SqlselectWithUrl extends WebAction {

	@TestData(reference = "Select-Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData databaseUrl;
	@TestData(reference = "variable-name", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData3;
	
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url = databaseUrl.getValue().toString();
		logger.info("url: "+url);
		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{
			logger.info("Connection start");
			Connection connection = databaseUtil.getConnection(url);
			logger.info("Connection done");
			Statement stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			ResultSet resultSet = stmt.executeQuery(query);
			StringBuilder resultStringBuilder = new StringBuilder();

			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			for (int i = 1; i <= columnCount; i++) {
				resultStringBuilder.append(metaData.getColumnName(i));
				if (i < columnCount) {
					resultStringBuilder.append(", ");
				}
			}
			resultStringBuilder.append("\n");

			while (resultSet.next()) {
				for (int i = 1; i <= columnCount; i++) {
					Object resultData = resultSet.getObject(i);
					if (resultData != null) {
						resultStringBuilder.append(resultData.toString());
					}
					else{
						resultStringBuilder.append("null");
					}
					resultStringBuilder.append(", ");
				}
				resultStringBuilder.append("\n");
			}
			if (resultStringBuilder.length() > 0) {
				resultStringBuilder.setLength(resultStringBuilder.length() - 2);
				String concatenatedResult = resultStringBuilder.toString();
				runTimeData = new com.testsigma.sdk.RunTimeData();
				runTimeData.setValue(concatenatedResult);
				runTimeData.setKey(testData3.getValue().toString());
				logger.info("Successfully Executed Select Query and Resultset is : " + concatenatedResult);
			}
			setSuccessMessage("Successfully Executed Select Query and stored in runtime variable: "+ testData3.getValue().toString());
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