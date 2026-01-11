package com.testsigma.addons.ios;

import com.testsigma.addons.oracledb.util.DatabaseUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.devtools.v135.io.IO;

import java.sql.*;

@Data
@Action(
        actionText = "Execute OracleDB Multiple Select-Queries on the connection DB_Connection_URL and store output into a variable-name",
        description = "Executes multiple Oracle SELECT queries, logs formatted result sets, and stores output in a runtime variable.",
        applicationType = ApplicationType.IOS
)
public class OracleMultipleQueriesSelect extends IOSAction {

    @TestData(reference = "Select-Queries")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "DB_Connection_URL")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {

        Result result = Result.SUCCESS;
        DatabaseUtil databaseUtil = new DatabaseUtil();

        Connection connection = null;
        Statement stmt = null;

        StringBuilder successMessage = new StringBuilder();
        StringBuilder runtimeValue = new StringBuilder();

        try {
            logger.info("====== Oracle DB Multiple Query Execution Started ======");

            connection = databaseUtil.getConnection(testData2.getValue().toString());
            stmt = connection.createStatement();

            String[] queries = testData1.getValue().toString().split(";");
            int queryCounter = 1;

            for (String rawQuery : queries) {

                String query = rawQuery.trim();
                if (query.isEmpty()) continue;

                logger.info("Executing Query [" + queryCounter + "]: " + query);

                ResultSet rs = stmt.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                /* ---------- UI OUTPUT ---------- */
                successMessage.append("<br><b>Query ")
                        .append(queryCounter)
                        .append(" Result: ")
                        .append(query)
                        .append("</b><br>");

                /* ---------- LOG OUTPUT ---------- */
                StringBuilder log = new StringBuilder();
                log.append("\nQuery ").append(queryCounter)
                        .append(" Result: ").append(query).append("\n");

                log.append("--------------------------------------------------------\n");

                // Headers
                StringBuilder headerRow = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    headerRow.append(rsmd.getColumnName(i));
                    if (i < columnCount) headerRow.append(" | ");
                }
                log.append(headerRow).append("\n");
                log.append("--------------------------------------------------------\n");

                // UI headers
                for (int i = 1; i <= columnCount; i++) {
                    successMessage.append(rsmd.getColumnName(i)).append(" | ");
                }
                successMessage.append("<br>");

                // Rows
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int j = 1; j <= columnCount; j++) {
                        String value = rs.getString(j);
                        String safe = value == null ? "" : value;

                        row.append(safe);
                        if (j < columnCount) row.append(" | ");

                        successMessage.append(safe).append(" | ");
                        runtimeValue.append(safe).append(",");
                    }
                    log.append(row).append("\n");
                    successMessage.append("<br>");
                }

                logger.info(log.toString());
                logger.info("Query [" + queryCounter + "] executed successfully");

                queryCounter++;
            }

            if (testData3 != null) {
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setKey(testData3.getValue().toString());
                runTimeData.setValue(runtimeValue.toString().replaceAll(",$", ""));
            }

            setSuccessMessage("Successfully executed all queries.<br>" + successMessage);
            logger.info("====== Oracle DB Multiple Query Execution Completed ======");

        } catch (Exception e) {
            setErrorMessage(ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }

        return result;
    }
}
