package com.testsigma.addons.web;


import com.testsigma.addons.mysql.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

@Data
@Action(actionText = "Execute MySQL Select-Query on the connection DB_Connection_URL and store the query result into a variable-name",
        description = "This Action executes a given Select Query and stores the query result into a provided runtime variable.",
        applicationType = ApplicationType.WEB)
public class Mysqlselectandstore extends WebAction {

    @TestData(reference = "Select-Query")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "DB_Connection_URL")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "variable-name" , isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;
    StringBuffer sb = new StringBuffer();

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        DatabaseUtil databaseUtil = new DatabaseUtil();
        try{
            Connection connection = databaseUtil.getConnection(testData2.getValue().toString());
            Statement stmt = connection.createStatement();
            String query = testData1.getValue().toString();
            ResultSet resultSet = stmt.executeQuery(query);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnNo = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnNo; i++) {
                sb.append(rsmd.getColumnName(i));
                sb.append(", ");
            }
            sb.append("<br>");
            while (resultSet.next()) {
                for (int j = 1; j <= columnNo; j++) {
                    if (j > 1) sb.append(", ");
                    String columnValue = resultSet.getString(j);
                    if (resultSet.wasNull()) {
                        sb.append("");
                    }
                    sb.append(columnValue);
                }
                sb.append("<br>");
            }
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(sb.toString());
            runTimeData.setKey(testData3.getValue().toString());
            setSuccessMessage("Successfully Executed Select Query and Resultset is : " + sb.toString());
            logger.info("Successfully Executed Select Query and Resultset is : " + sb.toString());
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