package com.testsigma.addons.restapi;

import com.testsigma.addons.mysql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.openqa.selenium.NoSuchElementException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;

@Data
@Action(actionText = "Execute MySQLScriptFile filepath on the Connection DB_Connection_URL",
description = "This action executes given scriptfile against the connection provided and prints the data.",
applicationType = ApplicationType.REST_API)
public class Mysqlscriptfile extends RestApiAction {

	@TestData(reference = "filepath")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
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
			String path = testData1.getValue().toString();

			sb.append("Successfully Executed ScriptFile and Result : " + "<br>");
			ScriptRunner sr = new ScriptRunner(connection);
			Reader reader = new BufferedReader(new FileReader(path));

			sr.runScript(reader);
			setSuccessMessage(sb.toString());
			logger.getValue();

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
			result = Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}