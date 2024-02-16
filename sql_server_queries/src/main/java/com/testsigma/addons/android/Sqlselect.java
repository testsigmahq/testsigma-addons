package com.testsigma.addons.android;

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
@Action(actionText = "SQLServer: Execute SQL Select-Query on the connection DB_Connection_URL for Database Database-Name using Username user-name and Password password and store output into a variable-name",
description = "This Action executes a given Select Query and stores the result(First cell data) into a provided runtime variable.",
applicationType = ApplicationType.ANDROID)
public class Sqlselect extends WebAction {

	@TestData(reference = "Select-Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "variable-name", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData3;
	@TestData(reference = "user-name")
	private com.testsigma.sdk.TestData userName;
	@TestData(reference = "password")
	private com.testsigma.sdk.TestData password;
	@TestData(reference = "Database-Name")
	private com.testsigma.sdk.TestData databaseName;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url =  String.format("jdbc:sqlserver://%s;databaseName=%s;user=%s;password=%s;encrypt=false;",testData2.getValue().toString(), databaseName.getValue().toString(), userName.getValue().toString(), password.getValue().toString());
		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{
			Connection connection = databaseUtil.getConnection(url);
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