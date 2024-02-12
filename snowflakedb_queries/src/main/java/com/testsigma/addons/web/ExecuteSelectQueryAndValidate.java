package com.testsigma.addons.web;

import com.testsigma.addons.util.SnowflakeDBConnection;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.ResultSet;
import java.sql.Statement;

@Data
@Action(actionText = "SnowflakeDB: Execute Select Query Select_Query on the connection db-connection-url and verify output is expected-value",
	description = "This Action executes a given Select Query and validates the result(First cell data) against the expected value.",
	applicationType = ApplicationType.WEB)
public class ExecuteSelectQueryAndValidate extends WebAction {

	@TestData(reference = "Select_Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "db-connection-url")
	private com.testsigma.sdk.TestData databaseUrl;
	@TestData(reference = "expected-value")
	private com.testsigma.sdk.TestData testData3;
	
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url = databaseUrl.getValue().toString();
		try{
			Statement stmt = SnowflakeDBConnection.getConnection(url);
			String query = testData1.getValue().toString();
			ResultSet resultSet = stmt.executeQuery(query);
			resultSet.next();
			String resultData = resultSet.getObject(1).toString();

			if(testData3.getValue().toString().equals(resultData)) {
				sb.append("<br>The output from the Select Query is matching with expected value.");
				sb.append("<br>Expected value:"+testData3.getValue().toString());
				sb.append("<br>Actual output from query:"+resultData);
				setSuccessMessage(sb.toString());
				logger.info(sb.toString());
			}
			else {
				result = Result.FAILED;
				sb.append("The selected query value not match with expected rows:" + "<br>");
				sb.append("Expected value of select query:"+testData3.getValue().toString() + "<br>");
				sb.append("Actual value from query execution:"+resultData + "<br>");
				setErrorMessage(sb.toString());
				logger.warn(sb.toString());
			}
		}
		catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			sb.append("<br>"+errorMessage);
			result = Result.FAILED;
			setErrorMessage(sb.toString());
			logger.warn(sb.toString());
		}
		return result;
	}
}