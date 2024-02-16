package com.testsigma.addons.mobileweb;

import com.testsigma.addons.util.DatabaseUtil;
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
@Action(actionText = "SQLServer: Execute SQL Query on the Connection DB_Connection_URL for Database Database-Name using Username user-name and Password password and verify affected rows count is Row-Count",
		description = "This Action executes given SQL query and validates the affected rows.",
		applicationType = ApplicationType.MOBILE_WEB)
public class SqlqueriesValidate extends WebAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "Row-Count")
	private com.testsigma.sdk.TestData testData3;
	@TestData(reference = "user-name")
	private com.testsigma.sdk.TestData userName;
	@TestData(reference = "password")
	private com.testsigma.sdk.TestData password;
	@TestData(reference = "Database-Name")
	private com.testsigma.sdk.TestData databaseName;
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url =  String.format("jdbc:sqlserver://%s;databaseName=%s;user=%s;password=%s;encrypt=false;",testData2.getValue().toString(), databaseName.getValue().toString(), userName.getValue().toString(), password.getValue().toString());
		DatabaseUtil databaseUtil = new DatabaseUtil();
		int rowsUpdatedOrFetched = 0;
		try{
			Connection connection = databaseUtil.getConnection(url);
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