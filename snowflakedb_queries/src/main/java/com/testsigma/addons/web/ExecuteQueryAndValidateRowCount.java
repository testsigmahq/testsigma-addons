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
@Action(actionText = "SnowflakeDB: Execute Query query-string on the Connection db-connection-url and verify affected rows count is row-count",
		description = "This Action executes given SQL query and validates the affected rows.",
		applicationType = ApplicationType.WEB)
public class ExecuteQueryAndValidateRowCount extends WebAction {

	@TestData(reference = "query-string")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "db-connection-url")
	private com.testsigma.sdk.TestData databaseUrl;
	@TestData(reference = "row-count")
	private com.testsigma.sdk.TestData testData3;
	
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url = databaseUrl.getValue().toString();

		int rowsUpdatedOrFetched = 0;
		try{

			Statement stmt = SnowflakeDBConnection.getConnection(url);
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
				result = Result.FAILED;
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
			result = Result.FAILED;
			setErrorMessage(sb.toString());
			logger.warn(sb.toString());
		}
		return result;
	}
}