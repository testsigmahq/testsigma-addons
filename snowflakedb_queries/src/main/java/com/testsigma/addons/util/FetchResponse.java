package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Objects;


public class FetchResponse {
    public static ResponseData execute(String url, String query, Logger logger) throws Exception {
        StringBuilder resultStringBuilder = new StringBuilder();
        String value = "";
        try {

            Statement statement = SnowflakeDBConnection.getConnection(url);
            logger.info("Successfully set JDBC_QUERY_RESULT_FORMAT='JSON'");
            logger.info("Executing query: " + query);
            ResultSet resultSet;
            resultSet = statement.executeQuery(query);
            resultStringBuilder = new StringBuilder();
            if (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                Object resultData = resultSet.getObject(1);
                String result = metaData.getColumnName(1) + ": " + resultData.toString();
                value = resultData.toString();
                resultStringBuilder.append(Objects.requireNonNullElse(result, "null"));
            }
            if (resultStringBuilder.length() > 0) {
                String concatenatedResult = resultStringBuilder.toString();
                logger.info("Successfully retrieved result of the given query: " + concatenatedResult);
            } else {
                logger.info("Response data is empty, response length: " + resultStringBuilder.length());
                throw new Exception("Response data is empty...");
            }
            statement.close();
            resultSet.close();
            return new ResponseData(resultStringBuilder.toString(), value);
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
            throw new Exception("Error occurred while executing the given query: " + errorMessage);
        }
    }
}
