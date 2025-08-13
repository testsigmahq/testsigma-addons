package com.testsigma.addons.restapi;

import com.testsigma.addons.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;

@Data
@Action(actionText = "SQLServer: Execute SQL Query on the Connection DB_Connection_URL for Database Database-Name using Username user-name and Password password",
description = "This action executes given query against the connection provided and prints the no. of affected/fetched rows.",
applicationType = ApplicationType.REST_API)
public class Sqlqueries extends RestApiAction {

	@TestData(reference = "Query")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "DB_Connection_URL")
	private com.testsigma.sdk.TestData testData2;
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
		Connection connection = null;
		Statement stmt = null;
		ResultSet resultSet = null;
			try {
				connection = databaseUtil.getConnection(url);
				stmt = connection.createStatement();
				String query = testData1.getValue().toString();
				resultSet = stmt.executeQuery(query);
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnNo = resultSet.getMetaData().getColumnCount();
				sb.append("Successfully Executed Query and Resultset is : " + "<br>");
				for (int i = 1; i <= columnNo; i++) {
					sb.append(rsmd.getColumnName(i));
					sb.append(", ");
				}
				sb.append("<br>");
				while (resultSet.next()) {
					for (int j = 1; j <= columnNo; j++) {
						if (j > 1)
							sb.append(", ");
						String columnValue = resultSet.getString(j);
						if (resultSet.wasNull()) {
							sb.append("");
						}
						sb.append(columnValue);
					}
					sb.append("<br>");
				}
				setSuccessMessage(sb.toString());
				logger.info(sb.toString());
			} catch (Exception e) {
				String errorMessage = ExceptionUtils.getStackTrace(e);
				result = Result.FAILED;
				setErrorMessage(errorMessage);
				logger.warn(errorMessage);
			} 
			finally {
			    // Close resources in finally block
			    try {
			        if (resultSet != null) {
			            resultSet.close();
			        }
			        if (stmt != null) {
			            stmt.close();
			        }
			        if (connection != null) {
			            connection.close();
			        }
			    } catch (SQLException se) {
			        logger.warn("Error closing resources: " + se.getMessage());
			    }
			}
		return result;
	}
}