package com.testsigma.addons.web;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(
        actionText = "Cassandra DB: Execute Cassandra query query-val on host host-name, port port-no using datacenter datacenter-val and keyspace keyspace-val, store results in runtime variable variable-name",
        description = "Connects to Cassandra database, executes a CQL query, and stores the results in runtime data",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class CassandraQueryAction extends WebAction {

    @TestData(reference = "host-name")
    private com.testsigma.sdk.TestData host;

    @TestData(reference = "port-no")
    private com.testsigma.sdk.TestData port;

    @TestData(reference = "datacenter-val")
    private com.testsigma.sdk.TestData datacenter;

    @TestData(reference = "keyspace-val")
    private com.testsigma.sdk.TestData keyspace;

    @TestData(reference = "query-val")
    private com.testsigma.sdk.TestData query;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating Cassandra query execution");

        String hostValue = host.getValue().toString();
        int portValue = Integer.parseInt(port.getValue().toString());
        String datacenterValue = datacenter.getValue().toString();
        String keyspaceValue = keyspace.getValue().toString();
        String queryValue = query.getValue().toString();

        logger.info("Connecting to Cassandra - Host: " + hostValue + ", Port: " + portValue +
                ", Datacenter: " + datacenterValue + ", Keyspace: " + keyspaceValue);
        logger.info("Query: " + queryValue);

        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(hostValue, portValue))
                .withLocalDatacenter(datacenterValue)
                .withKeyspace(keyspaceValue)
                .build()) {

            logger.info("Connected to Cassandra!");

            ResultSet rs = session.execute(queryValue);
            List<String> results = new ArrayList<>();

            for (Row row : rs) {
                StringBuilder rowData = new StringBuilder("{");
                List<String> fieldValues = new ArrayList<>();

                for (ColumnDefinition def : row.getColumnDefinitions()) {
                    String columnName = def.getName().asCql(true);
                    Object columnValue = row.getObject(columnName);
                    String value = (columnValue != null) ? columnValue.toString() : "null";
                    fieldValues.add(columnName + ": " + value);
                }

                rowData.append(String.join(", ", fieldValues)).append("}");
                results.add(rowData.toString());
                logger.info("Row: " + rowData);
            }

            String resultsString = String.join("\n", results);
            runTimeData.setValue(resultsString);
            runTimeData.setKey(variableName.getValue().toString());

            setSuccessMessage("Successfully executed query and retrieved " + results.size() +
                    " row(s). Query completed successfully. Result: " + resultsString);
            logger.info("Query completed successfully. Retrieved " + results.size() + " row(s).");

        } catch (Exception e) {
            logger.warn("Error executing Cassandra query: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Failed to execute Cassandra query: " + ExceptionUtils.getMessage(e));
        }

        return result;
    }
}
