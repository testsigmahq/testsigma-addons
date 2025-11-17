package com.testsigma.addons.web;

import com.testsigma.addons.postgresql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
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
@Action(actionText = "Execute PostgreSQL Query on the Connection DB_Connection_URL",
description = "This action executes given query against the connection provided and prints the no. of affected/fetched rows.",
applicationType = ApplicationType.WEB)

public class PostgreSQLqueries extends WebAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		DatabaseUtil databaseUtil = new DatabaseUtil();
		String connectionUrl = testData2.getValue().toString();
		String query = testData1.getValue().toString();
		
		logger.info("Initiating execution");
		logger.info("Executing the Query");
		logger.info("Connection URL:" + databaseUtil.maskConnectionUrl(connectionUrl));

		try (Connection connection = databaseUtil.getConnection(connectionUrl);
			 Statement stmt = connection.createStatement();
			 ResultSet resultSet = stmt.executeQuery(query)) {
			
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnNo = rsmd.getColumnCount();
			StringBuilder sb = new StringBuilder();
			sb.append("Successfully Executed Query and Resultset is : ").append("<br>");
			
			for (int i = 1; i <= columnNo; i++) {
				if (i > 1) sb.append(", ");
				sb.append(rsmd.getColumnName(i));
			}
			sb.append("<br>");
			
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
				sb.append("<br>");
			}
			
			String message = sb.toString();
			setSuccessMessage(message);
			logger.info("Query executed successfully");
		}
		catch (Exception e){
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Error executing query: " + e.getMessage());
			logger.warn("Error executing query: " + e.getMessage());
		}
		return result;
	}
}