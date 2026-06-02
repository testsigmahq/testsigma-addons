package com.testsigma.addons.web;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import com.testsigma.addons.Utils.MongoOperation;
import com.testsigma.addons.Utils.MongoOperationFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute a MongoDB Query query on the specified Database DBname using MongoDB Connection MongoDB_Connection",
        description = "Executes a MongoDB query on the database and retrieves results",
        applicationType = ApplicationType.WEB)
public class MongoDB_QueryExecutor extends WebAction {
    @TestData(reference = "DBname")
    private com.testsigma.sdk.TestData DBname;

    @TestData(reference = "MongoDB_Connection")
    private com.testsigma.sdk.TestData connectionURL;

    @TestData(reference = "query")
    private com.testsigma.sdk.TestData query;

    StringBuffer sb = new StringBuffer();

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        MongoClient mongoClient = null;

        try {
            // MongoDB connection
            String connection = connectionURL.getValue().toString();
            MongoClientURI uri = new MongoClientURI(connection);
            mongoClient = new MongoClient(uri);
            MongoDatabase database = mongoClient.getDatabase(DBname.getValue().toString());

            // Select operation based on query input
            String queryInput = query.getValue().toString();

            MongoOperation operation = MongoOperationFactory.getOperation(queryInput);

            if (operation != null) {
                    result = operation.execute(database, queryInput, sb, logger);
                    logger.info(queryInput + " : Query executed");

                    // Extract only the JSON array part from the result
                    String resultString = sb.toString();

                    setSuccessMessage("Query executed successfully. " + resultString);
                    logger.info("Query executed successfully.These are the updated values: : " + resultString);

            } else {
                setErrorMessage("Unsupported query operation. Only 'find', 'insert', 'update', 'delete', 'aggregate' and 'index' queries are supported. Query: " + queryInput);
                result = Result.FAILED;
            }
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = Result.FAILED;
            setErrorMessage(errorMessage);
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }
        return result;
    }
}


