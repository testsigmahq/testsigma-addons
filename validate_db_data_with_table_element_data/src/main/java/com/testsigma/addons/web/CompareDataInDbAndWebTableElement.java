package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import java.sql.*;
import java.util.List;
@Data
@Action(actionText = "Execute query: query_ in the database db with connection url: url_, username: username_"+
        " and password: password_ to fetch the data and compare it with data option case-sensitivity in table element table_element",
        description = "Verifies data from DB and web table element are equal or not",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class CompareDataInDbAndWebTableElement extends WebAction {

    @TestData(reference = "url_")
    private com.testsigma.sdk.TestData url_;
    @TestData(reference = "username_")
    private com.testsigma.sdk.TestData username_;
    @TestData(reference = "password_")
    private com.testsigma.sdk.TestData password_;
    @TestData(reference = "db",allowedValues = {"Oracle","MySQL","PostgreSQL"})
    private com.testsigma.sdk.TestData db;
    @TestData(reference = "option",allowedValues = {"with","without"})
    private com.testsigma.sdk.TestData option;
    @TestData(reference = "query_")
    private com.testsigma.sdk.TestData query_;
    @Element(reference = "table_element")
    private com.testsigma.sdk.Element element;
    private static final String COLUMN_COUNT_MISMATCH = "Column count in DB data and element data mismatched.\n" +
            " Column count in DB : %d,\n Column count in table element : %d.";
    private static final String ROW_COUNT_MISMATCH = "Row count in DB data and element data mismatched.\n" +
            " Row count in DB : %d,\n Row count in table element : %d.";
    private static final String DATA_MISMATCH = "Data mismatched at column - %d and row - %d.\n" +
            " Data in DB : %s,\n Data in table element : %s.";
    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        WebElement table = element.getElement();
        String url = url_.getValue().toString();
        String username = username_.getValue().toString();
        String password = password_.getValue().toString();
        String query = query_.getValue().toString();
        Connection connection = null;
        Statement statement = null;
        try {
            getClassForDb(db.getValue().toString());
            connection = DriverManager.getConnection(url,username, password);
            logger.info("Opened database successfully");
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(query);
            logger.info("Query executed successfully");
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<WebElement> tableRows = getRows(table);
            List<WebElement> tableHeaders = getHeaders(tableRows.get(0));
            int tableColumnCount = tableHeaders.size();
            resultSet.last();
            checkColumnAndRowCount(metaData.getColumnCount(),tableColumnCount,resultSet.getRow(),(tableRows.size() - 1));
            logger.info("Both data sizes are equal");
            resultSet.beforeFirst();
            checkData(tableRows,resultSet,tableColumnCount,option.getValue().toString());
            logger.info("Data in both DB and web table element are equal");
            resultSet.close();
            statement.close();
            connection.close();
            logger.info("Connection closed");
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.debug("Execution failed. "+e.getMessage());
            setErrorMessage(e.getMessage());
        }
        setSuccessMessage("Data in both DB and web table element are equal");
        return result;
    }
    protected List<WebElement> getRows(WebElement table){
        return table.findElements(By.tagName("tr"));
    }

    protected List<WebElement> getHeaders(WebElement row){
        return row.findElements(By.tagName("th"));
    }

    protected List<WebElement> getCellsOfRow(WebElement row){
        return row.findElements(By.tagName("td"));
    }

    protected Class<?> getClassForDb(String db) throws Exception{
        switch (db){
            case "Oracle":
                return Class.forName("oracle.jdbc.driver.OracleDriver");
            case "MySQL":
                return Class.forName("com.mysql.cj.jdbc.Driver");
            case "PostgreSQL":
                return Class.forName("org.postgresql.Driver");
            default:
                return null;
        }
    }
    protected void checkColumnAndRowCount (int dbColumnCount, int tableColumnCount, int dbRowCount, int tableRowCount)
            throws Exception{
        if(dbColumnCount != tableColumnCount)
            throw new Exception(String.format(COLUMN_COUNT_MISMATCH,dbColumnCount,tableColumnCount));
        if(dbRowCount != tableRowCount)
            throw new Exception(String.format(ROW_COUNT_MISMATCH,dbRowCount,tableRowCount));
    }
    protected void checkData(List<WebElement> tableRows, ResultSet resultSet, int columnCount, String option) throws Exception{
        int rowIndex = 1;
        if(option.equals("with")){
            while (resultSet.next()) {
                List<WebElement> cells = getCellsOfRow(tableRows.get(rowIndex));
                for (int i = 0; i < columnCount; i++) {
                    if(!cells.get(i).getText().equals(resultSet.getString(i + 1)))
                        throw new Exception(String.format(DATA_MISMATCH,i + 1,rowIndex,resultSet.getString(i + 1),cells.get(i).getText()));
                }
                rowIndex++;
            }
        } else {
            while (resultSet.next()) {
                List<WebElement> cells = getCellsOfRow(tableRows.get(rowIndex));
                for (int i = 0; i < columnCount; i++) {
                    if(!cells.get(i).getText().equalsIgnoreCase(resultSet.getString(i + 1)))
                        throw new Exception(String.format(DATA_MISMATCH,i + 1,rowIndex,resultSet.getString(i + 1),cells.get(i).getText()));
                }
                rowIndex++;
            }
        }
    }
}

