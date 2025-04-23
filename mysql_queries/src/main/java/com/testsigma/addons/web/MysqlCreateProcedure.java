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
@Action(actionText = "Execute MySQL Create Procedure Query on the Connection DB_Connection_URL",
description = "This action executes given query against the connection and creates the procedure",
applicationType = ApplicationType.WEB)
public class MysqlCreateProcedure extends WebAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		DatabaseUtil databaseUtil = new DatabaseUtil();

		String httpNonProxyHosts = System.getProperty("http.nonProxyHosts");
		String httpsNonProxyHosts = System.getProperty("https.nonProxyHosts");
		String socksNonProxyHosts = System.getProperty("socksNonProxyHosts");

		try{
			// Temporarily unset proxy properties to avoid issues
			System.setProperty("http.nonProxyHosts", "");
			System.setProperty("https.nonProxyHosts", "");
			System.setProperty("socksNonProxyHosts", "");

			Connection connection = databaseUtil.getConnection(testData2.getValue().toString());
			Statement stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			boolean resultSet = stmt.execute(query);
			
			setSuccessMessage("The procedure is created successfully: " +query);
			logger.info("The procedure is created successfully: " +query);

			// Restore original proxy system properties if they were not null
			if (httpNonProxyHosts != null) {
				System.setProperty("http.nonProxyHosts", httpNonProxyHosts);
			}
			if (httpsNonProxyHosts != null) {
				System.setProperty("https.nonProxyHosts", httpsNonProxyHosts);
			}
			if (socksNonProxyHosts != null) {
				System.setProperty("socksNonProxyHosts", socksNonProxyHosts);
			}
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