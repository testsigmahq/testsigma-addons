package com.testsigma.addons.ios;

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
@Action(actionText = "Execute SQL Query on the Connection DB_Connection_URL",
description = "This action executes given query against the connection provided and prints the no. of affected/fetched rows.",
applicationType = ApplicationType.IOS)
public class SqlqueriesWithUrl extends WebAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData databaseUrl;
	

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url = databaseUrl.getValue().toString();
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
				setSuccessMessage("Successfully Executed Database Query, No. of Rows fetched from DB : " +rowsUpdatedOrFetched);
				logger.info("Successfully Executed Database Query, No. of Rows fetched from DB : " +rowsUpdatedOrFetched);
			}else {
				rowsUpdatedOrFetched = stmt.executeUpdate(query);
				setSuccessMessage("Successfully Executed Database Query, No. of rows affected in DB : " +rowsUpdatedOrFetched);
				logger.info("Successfully Executed Database Query, No. of Rows fetched from DB : " +rowsUpdatedOrFetched);
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