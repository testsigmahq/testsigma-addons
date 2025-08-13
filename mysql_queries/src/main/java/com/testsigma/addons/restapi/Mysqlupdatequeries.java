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
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.Statement;

@Data
@Action(actionText = "Execute MySQL Update Query on the Connection DB_Connection_URL",
description = "This action executes given update query against the connection provided and prints the no. of affected/fetched rows.",
applicationType = ApplicationType.REST_API)
public class Mysqlupdatequeries extends RestApiAction {

	@TestData(reference = "Query")
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
			Statement stmt = connection.createStatement();
			String query = testData1.getValue().toString();
			int resultdata = stmt.executeUpdate(query);

			sb.append("Successfully Executed Query and affected rows are : " + "<br>" +resultdata);
			
			setSuccessMessage(sb.toString());
			logger.info(sb.toString());

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