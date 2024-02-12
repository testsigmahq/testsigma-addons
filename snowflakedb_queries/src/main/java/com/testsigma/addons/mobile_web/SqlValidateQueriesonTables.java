package com.testsigma.addons.mobile_web;

import com.testsigma.addons.util.SnowflakeDBConnection;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Data
@Action(actionText = "SnowflakeDB: Validate Query query1 and compare with the Query query2 from the Connection db-connection-url",
description = "This action validate queries on the database connections",
applicationType = ApplicationType.MOBILE_WEB)
public class SqlValidateQueriesonTables extends WebAction {

	@TestData(reference = "query1")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "query2")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "db-connection-url")
	private com.testsigma.sdk.TestData databaseUrl;
	
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		String url = databaseUrl.getValue().toString();

		try{
			Statement stmt1 = SnowflakeDBConnection.getConnection(url);
			Statement stmt2 = SnowflakeDBConnection.getConnection(url);

			String query1 = testData1.getValue().toString();
			String query2 = testData2.getValue().toString();
			
			ResultSet resultSet1 = stmt1.executeQuery(query1);
			ResultSet resultSet2 = stmt2.executeQuery(query2);

			boolean metadataComparisonSuccess = compareMetadata(resultSet1,resultSet2);
			if(!metadataComparisonSuccess) {
				return Result.FAILED;
			}
			boolean dataComparisonSuccess = compareQueryData(resultSet1, resultSet2);
			if(!dataComparisonSuccess) {
				return Result.FAILED;
			}
			logger.info(sb.toString());
		}catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		setSuccessMessage("The two queries are have similar data");
		logger.info("The two queries are have similar data");
		logger.info(sb.toString());
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
						sb.append("Column name from Query-1:" +"'" +rsmd1.getColumnName(i) + "'" + "and Column name from Query-2:" + "'" +rsmd2.getColumnName(i) +"'");
						setErrorMessage(sb.toString());
						logger.warn(sb.toString());
						return false;
					}
				}
			}
			return true;
		}
		sb.append("The Columns count does not match:<br>");
		sb.append("Columns from Query-1:" + "'").append(columnNo1).append("'").append("and Columns from Query-2:").append("'").append(columnNo2).append("'");
		setErrorMessage(sb.toString());
		return false;
	}
	private boolean compareQueryData(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {
		int columnNo1 = resultSet1.getMetaData().getColumnCount();

		CachedRowSet cachedResultSet1 = RowSetProvider.newFactory().createCachedRowSet();
		CachedRowSet cachedResultSet2 = RowSetProvider.newFactory().createCachedRowSet();

		cachedResultSet1.populate(resultSet1);
		cachedResultSet2.populate(resultSet2);

		while (cachedResultSet1.next() && cachedResultSet2.next()) {
			for (int i = 1; i <= columnNo1; i++) {
				if (!Objects.equals(cachedResultSet1.getObject(i), cachedResultSet2.getObject(i))) {
					sb.append("The values do not match for column: Column Position(Starting from 1) ").append(i).append(" Row Number: ").append(cachedResultSet1.getRow());
					sb.append("<br>");
					sb.append("Value from Query-1: '").append(cachedResultSet1.getObject(i)).append("' and Value from Query-2: '").append(cachedResultSet2.getObject(i)).append("'");
					setErrorMessage(sb.toString());
					logger.warn(sb.toString());
					return false;
				}
			}
		}

		// Log the contents of both queries when values match
		StringBuilder successLog = new StringBuilder("Values match for all columns. Contents of both queries:\n");

		cachedResultSet1.beforeFirst();
		cachedResultSet2.beforeFirst();

		// Log the contents of cachedResultSet1
		successLog.append("Query-1:\n");
		while (cachedResultSet1.next()) {
			successLog.append(cachedResultSet1.getRow()).append(": ");
			for (int i = 1; i <= columnNo1; i++) {
				successLog.append(cachedResultSet1.getObject(i)).append("\n");
			}
			successLog.append("\n");
		}

		// Log the contents of cachedResultSet2
		successLog.append("\nQuery-2:\n");
		while (cachedResultSet2.next()) {
			successLog.append(cachedResultSet2.getRow()).append(": ");
			for (int i = 1; i <= columnNo1; i++) {
				successLog.append(cachedResultSet2.getObject(i)).append("\n");
			}
			successLog.append("\n");
		}

		logger.info(successLog.toString());

		return true;
	}
}