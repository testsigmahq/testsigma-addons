package com.testsigma.addons.android;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.openqa.selenium.NoSuchElementException;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Execute PostgreSQL Select-Query on the connection DB_Connection_URL and store output into a variable-name",
description = "This Action executes a given Select Query and stores the result(First cell data) into a provided runtime variable.",
applicationType = ApplicationType.ANDROID)

public class PostgreSQLselect extends AndroidAction {

	@TestData(reference = "Select-Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "variable-name")
	private com.testsigma.sdk.TestData testData3;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		DatabaseUtil databaseUtil = new DatabaseUtil();
		String connectionUrl = testData2.getValue().toString();
		String query = testData1.getValue().toString();
		String variableName = testData3.getValue().toString();
		
		logger.info("Initiating execution");
		logger.info("Executing the Select Query");
		logger.info("Connection URL:" + databaseUtil.maskConnectionUrl(connectionUrl));
		logger.info("Variable Name:" + variableName);

		try (Connection connection = databaseUtil.getConnection(connectionUrl);
			 Statement stmt = connection.createStatement();
			 ResultSet resultSet = stmt.executeQuery(query)) {
			
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = rsmd.getColumnCount();
			StringBuilder sb = new StringBuilder();
			
			while (resultSet.next()) {
				for (int j = 1; j <= columnNo; j++) {
					if (j > 1) sb.append(", ");
					String columnValue = resultSet.getString(j);
					if (resultSet.wasNull()) {
						sb.append("");
					} else {
						sb.append(columnValue);
					}
				}
			}
			
			String resultValue = sb.toString();
			runTimeData = new com.testsigma.sdk.RunTimeData();
			runTimeData.setValue(resultValue);
			runTimeData.setKey(variableName);
			setSuccessMessage("Successfully Executed Select Query and Resultset is : " + resultValue);
			logger.info("Successfully Executed Select Query");
		}
		catch (Exception e){
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Error executing query: " + e.getMessage());
			logger.warn("Error executing query: " + e.getMessage());
		}
		return result;
	}
}
