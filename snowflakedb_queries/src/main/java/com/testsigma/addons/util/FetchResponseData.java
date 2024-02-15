package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Objects;

public class FetchResponseData {
    public static String execute(String url, String query, Logger logger) throws Exception {
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            StringBuilder resultStringBuilder = null;
            statement = SnowflakeDBConnection.getConnection(url);
            logger.info("Successfully set JDBC_QUERY_RESULT_FORMAT='JSON'");
            logger.info("Executing query: " + query);
            resultSet = statement.executeQuery(query);
            resultStringBuilder = new StringBuilder();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                resultStringBuilder.append(metaData.getColumnName(i));
                if (i < columnCount) {
                    resultStringBuilder.append(", ");
                }
            }
            resultStringBuilder.append("\n");
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    Object resultData = resultSet.getObject(i);
                    if (resultData != null) {
                        resultStringBuilder.append(resultData.toString());
                    } else {
                        resultStringBuilder.append("null");
                    }
                    resultStringBuilder.append(", ");
                }
                resultStringBuilder.append("\n");
            }
            statement.close();
            resultSet.close();
            return resultStringBuilder.toString();
        }
        catch (Exception e){
            Objects.requireNonNull(statement).close();
            Objects.requireNonNull(resultSet).close();
            throw new Exception(e);
        }
    }
}
