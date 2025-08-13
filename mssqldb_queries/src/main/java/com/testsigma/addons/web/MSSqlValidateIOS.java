package com.testsigma.addons.web;

import com.testsigma.addons.mssqldb.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Data
@Action(actionText = "Execute MSSqlDB Select-Query on the connection DB_Connection_URL and verify output contains testdata",
description = "This Action executes a given Query and verify the output",
applicationType = ApplicationType.IOS)
public class MSSqlValidateIOS extends WebAction {

	@TestData(reference = "Select-Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "testdata")
	private com.testsigma.sdk.TestData testData3;
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution" + "<br>");
		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{
			Connection connection = databaseUtil.getConnection(testData2.getValue().toString());
			Statement stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			ResultSet resultSet = stmt.executeQuery(query);
			int columnNo = resultSet.getMetaData().getColumnCount();
			int count = 0;
			while (resultSet.next()) {
				for (int i = 1; i <= columnNo; i++) {
					String columnValue = resultSet.getString(i);
					if(columnValue != null)
					{
						if(columnValue.contains(testData3.getValue().toString()))
						{
							count++;
						}
					}
				}
			}
			if(count >= 1)
			{
				sb.append("<br>The output from the Query is matching with expected value.");
				sb.append("<br>Expected value:"+testData3.getValue().toString());
				setSuccessMessage(sb.toString());
				logger.info(sb.toString());
			}
			else
			{
				result = Result.FAILED;
				sb.append("The selected query value not match with expected rows:" + "<br>");
				sb.append("Expected value :"+testData3.getValue().toString() + "<br>");
				setErrorMessage(sb.toString());
				logger.warn(sb.toString());
			}

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