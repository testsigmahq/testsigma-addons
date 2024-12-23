package com.testsigma.addons.android;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.addons.mysql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute MySQL Query on the Connection DB_Connection_URL and verify affected rows count is Row-Count",
description = "This Action executes given SQL query and validates the affected rows.",
applicationType = ApplicationType.ANDROID)
public class MysqlqueriesValidate extends WebAction {

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
		DatabaseUtil databaseUtil = new DatabaseUtil();
		int rowsUpdatedOrFetched = 0;
		try{
			Connection connection = databaseUtil.getConnection(testData2.getValue().toString());
			Statement stmt = connection.createStatement();
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
		}
		return result;
	}
}