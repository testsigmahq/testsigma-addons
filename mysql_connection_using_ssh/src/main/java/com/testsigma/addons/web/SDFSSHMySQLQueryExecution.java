package com.testsigma.addons.web;

import com.jcraft.jsch.*;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "SSH: Execute MySQL Query via SSH Tunnel, SSH Server details: Host: Host-Name, Port: Port-Number, UserName: User-Name, Password: User-Password, Local Port: Local-Port, Remote Host: Remote-Host, Remote Port: Remote-Port, DB User: DB-User, DB Password: DB-Password, DB Name: DB-Name, SQL Query: SQL-Query, Store result in a runtime variable variable-name",
        description = "Creates an SSH tunnel, establishes a MySQL connection, and executes a MySQL query.Host-Name: The hostname or IP address of your SSH server. Port-Number: The SSH port number (usually 22). User-Name: The SSH username. User-Password: The SSH password.Local-Port: The local port on your machine that will be used for the tunnel (e.g., 3307). Remote-Host: The hostname or IP address of your MySQL server (often localhost if the database is on the same machine as the SSH server).Remote-Port: The port number of your MySQL server (usually 3306). DB-User: The MySQL database username.DB-Password: The MySQL database password.DB-Name: The MySQL database name.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SDFSSHMySQLQueryExecution extends WebAction {

    @TestData(reference = "Host-Name")
    private com.testsigma.sdk.TestData hostName;
    @TestData(reference = "Port-Number")
    private com.testsigma.sdk.TestData portNumber;
    @TestData(reference = "User-Name")
    private com.testsigma.sdk.TestData userName;
    @TestData(reference = "User-Password")
    private com.testsigma.sdk.TestData userPassword;
    @TestData(reference = "Local-Port")
    private com.testsigma.sdk.TestData localPort;
    @TestData(reference = "Remote-Host")
    private com.testsigma.sdk.TestData remoteHost;
    @TestData(reference = "Remote-Port")
    private com.testsigma.sdk.TestData remotePort;
    @TestData(reference = "DB-User")
    private com.testsigma.sdk.TestData dBUsername;
    @TestData(reference = "DB-Password")
    private com.testsigma.sdk.TestData dBPassword;
    @TestData(reference = "DB-Name")
    private com.testsigma.sdk.TestData databaseName;
    @TestData(reference = "SQL-Query")
    private com.testsigma.sdk.TestData sqlQuery;
    @TestData(reference = "variable-name")
    private com.testsigma.sdk.TestData storeVariable;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    private Session session = null;
    private Connection conn = null;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating single MySQL query execution via SSH tunnel.");

        String sshHost = hostName.getValue().toString();
        String sshUser = userName.getValue().toString();
        String sshPassword = userPassword.getValue().toString();
        int sshPort = Integer.parseInt(portNumber.getValue().toString());
        int lport = Integer.parseInt(localPort.getValue().toString());
        int rport = Integer.parseInt(remotePort.getValue().toString());
        String rhost = remoteHost.getValue().toString();
        String dbuserName = dBUsername.getValue().toString();
        String dbpassword = dBPassword.getValue().toString();
        String dbName = databaseName.getValue().toString();
        String query = sqlQuery.getValue().toString();


        String url = "jdbc:mysql://localhost:" + lport + "/" + dbName + "?useSSL=false";

        try {
            // Establish SSH Tunnel and Database Connection
            if (!establishSshTunnel(sshHost, sshUser, sshPassword, sshPort, lport, rhost, rport)) {
                return Result.FAILED; // Error already set within establishSshTunnel
            }


            if (!establishDatabaseConnection(url, dbuserName, dbpassword)) {
                return Result.FAILED; // Error already set within establishDatabaseConnection
            }


            // Execute SQL query
            logger.info("query : " + query);
            String queryResult = executeQuery(query);

            if (queryResult.startsWith("Error executing SQL query")) {
                return Result.FAILED; // Error already set within executeQuery
            }

            setSuccessMessage("SQL query executed successfully via SSH tunnel. Result: " + queryResult);
            runTimeData.setKey(storeVariable.getValue().toString());
            runTimeData.setValue(queryResult);
            return Result.SUCCESS;

        } finally {
            closeResources();
        }
    }

    private boolean establishSshTunnel(String sshHost, String sshUser, String sshPassword, int sshPort, int lport, String rhost, int rport) {
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPassword);
            session.setConfig(config);
            session.connect(30000);
            session.setPortForwardingL(lport, rhost, rport);
            return true;
        } catch (JSchException e) {
            String errorStack = ExceptionUtils.getStackTrace(e);
            logger.warn("Error establishing SSH tunnel: " + errorStack);
            setErrorMessage("Error establishing SSH tunnel: " + e.getMessage());
            return false;
        }
    }

    private boolean establishDatabaseConnection(String url, String dbuserName, String dbpassword) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, dbuserName, dbpassword);
            if (conn == null || conn.isClosed()){
                setErrorMessage("Database connection failed.");
                return false;
            }
            return true;
        } catch (ClassNotFoundException e) {
            logger.warn("MySQL JDBC Driver not found: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("MySQL JDBC Driver not found: " + e.getMessage());
            return false;
        }
        catch (SQLException e) {
            logger.warn("Error establishing database connection: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error establishing database connection: " + e.getMessage());
            return false;
        }
    }

    private String executeQuery(String query) {
        Statement statement = null;
        StringBuilder resultString = new StringBuilder();
        try {
            statement = conn.createStatement();
            String trimmedQuery = query.trim();
            if (!trimmedQuery.isEmpty()) {
                boolean isResultSet = statement.execute(trimmedQuery);
                if (isResultSet) {
                    ResultSet resultSet = statement.getResultSet();
                    List<String> rowValues;
                    List<List<String>> resultList = new ArrayList<>();
                    while (resultSet.next()){
                        rowValues = new ArrayList<>();
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++){
                            rowValues.add(resultSet.getString(i));
                        }
                        resultList.add(rowValues);
                    }
                    // Convert resultList to a string
                    for (List<String> row : resultList) {
                        resultString.append(String.join(",", row)).append("\n"); // Use a comma to separate columns in a row
                    }
                    if (resultString.length() > 0) {
                        //Remove the last appended new line character
                        resultString.deleteCharAt(resultString.length() - 1);
                    }

                }else {
                    // Log the update count
                    int updateCount = statement.getUpdateCount();
                    resultString.append("Update count: ").append(updateCount);
                    if (updateCount == 0) {
                        resultString.append(" , Query doesn't affect any rows or is a DDL query" );
                    }
                }
            }

        } catch (SQLException e) {
            String errorStack = ExceptionUtils.getStackTrace(e);
            //Check if the SQL Exception is due to a syntax error
            if (e.getSQLState() != null && e.getSQLState().startsWith("42")) {
                logger.warn("SQL Syntax Error: " + errorStack);
                setErrorMessage("SQL Syntax Error: " + e.getMessage());
                return "Error executing SQL query:" + e.getMessage();
            }
            logger.warn("Error occurred while executing query: " + errorStack);
            setErrorMessage("Error executing SQL query: " + e.getMessage());
            return "Error executing SQL query:" + e.getMessage();
        } finally {
            try {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                logger.warn("Error closing statement: "+e.getMessage());
                setErrorMessage("Error closing statement: "+e.getMessage()); // Log but don't fail the query because we already got data or error.
            }
        }
        return resultString.toString();
    }

    private void closeResources() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.warn("Error closing database connection: " + e.getMessage());
            setErrorMessage("Error closing database connection: " + e.getMessage());
        }

        if (session != null && session.isConnected()) {
            session.disconnect();
        }

    }
}


