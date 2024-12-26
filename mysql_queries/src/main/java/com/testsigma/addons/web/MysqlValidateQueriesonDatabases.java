package com.testsigma.addons.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.addons.mysql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Validate MySQL Query on the Connection DB_Connection_URL1 and compare with data from the Connection DB_Connection_URL2",
description = "This action validate queries of the two database connections",
applicationType = ApplicationType.WEB)
public class MysqlValidateQueriesonDatabases extends WebAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL1")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "DB_Connection_URL2")
	private com.testsigma.sdk.TestData testData3;
	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		DatabaseUtil databaseUtil = new DatabaseUtil();
		try{

			Connection connection1 = databaseUtil.getConnection(testData2.getValue().toString());
			Connection connection2 = databaseUtil.getConnection(testData3.getValue().toString());

			Statement stmt1 = connection1.createStatement();
			Statement stmt2 = connection2.createStatement();
			String query = testData1.getValue().toString();
			ResultSet resultSet1 = stmt1.executeQuery(query);
			ResultSet resultSet2 = stmt2.executeQuery(query);

			boolean metadataComparisonSuccess = compareMetadata(resultSet1,resultSet2);
			if(!metadataComparisonSuccess) {
				return com.testsigma.sdk.Result.FAILED;
			}
			boolean dataComparisonSuccess = compareQueryData(resultSet1, resultSet2);
			if(!dataComparisonSuccess) {
				return com.testsigma.sdk.Result.FAILED;
			}
		}catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		setSuccessMessage("The two databases are have similar data");
		logger.info("The two databases are have similar data");
		return result;
	}

	private boolean compareMetadata(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {

		ResultSetMetaData rsmd1 = resultSet1.getMetaData();
		ResultSetMetaData rsmd2 = resultSet2.getMetaData();

		int columnNo1 = resultSet1.getMetaData().getColumnCount();
		int columnNo2 = resultSet2.getMetaData().getColumnCount();

		if(columnNo1 == columnNo2)
		{ 
			if(columnNo1 != 1)
			{
				for(int i=1;i<=columnNo1;i++) {
					if (!rsmd1.getColumnName(i).equals(rsmd2.getColumnName(i))) {
						sb.append("The ColumnNames does not match:<br>");
						sb.append("Column name from Connection-1:" +"'" +rsmd1.getColumnName(i) + "'" + "and Column name from Connection-2:" + "'" +rsmd2.getColumnName(i) +"'");
						setErrorMessage(sb.toString());
						logger.warn(sb.toString());
						return false;
					}
				}
			}
			return true;
		}
		sb.append("The Columns count does not match:<br>");
		sb.append("Columns from Connection-1:" +"'" +columnNo1 + "'" + "and Columns from Connection-2:" + "'" +columnNo2 +"'");
		setErrorMessage(sb.toString());
		logger.info(sb.toString());
		return false;
	}
	private boolean compareQueryData(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {

		int columnNo1 = resultSet1.getMetaData().getColumnCount();

		while (resultSet1.next() && resultSet2.next())
		{	
			for (int i = 1; i <= columnNo1; i++) {
				if (!resultSet1.getObject(i).equals(resultSet2.getObject(i))) {
					sb.append("The values does not match for column:Column Position(Starting from 1)"+i+" Row Number:"+resultSet1.getRow());
					sb.append("<br>");
					sb.append("value from Connection-1:" +"'" +resultSet1.getObject(i) +"'" + "and value from Connection-2:" + "'" +resultSet2.getObject(i) +"'");
					setErrorMessage(sb.toString());
					logger.warn(sb.toString());
					return false;
				}
			}
			if ((resultSet2.isLast() == true && resultSet1.isLast() == false) || (resultSet2.isLast() == false && resultSet1.isLast() == true)) { 
				setErrorMessage("No of rows does not match.");
				logger.warn("No. of rows from the query does not match.");
				return false;
			}
		}
		return true;
	}
}