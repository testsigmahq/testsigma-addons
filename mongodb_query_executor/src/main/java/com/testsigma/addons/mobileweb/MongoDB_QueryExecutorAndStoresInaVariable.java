package com.testsigma.addons.mobileweb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.testsigma.addons.Utils.MongoOperation;
import com.testsigma.addons.Utils.MongoOperationFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute a MongoDB Query query on the specified Database DBname using MongoDB Connection MongoDB_Connection and store result in variable",
        description = "Executes a MongoDB query on the database, retrieves results and stores in runtime variable",
        applicationType = ApplicationType.MOBILE_WEB)
public class MongoDB_QueryExecutorAndStoresInaVariable extends WebAction {

    @TestData(reference = "DBname")
    private com.testsigma.sdk.TestData DBname;

    @TestData(reference = "MongoDB_Connection")
    private com.testsigma.sdk.TestData connectionURL;

    @TestData(reference = "query")
    private com.testsigma.sdk.TestData query;

    @TestData(reference = "variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVar;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    StringBuffer sb = new StringBuffer();

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        MongoClient mongoClient = null;
        try {
            // MongoDB connection setup
            String connection = connectionURL.getValue().toString();
            MongoClientURI uri = new MongoClientURI(connection);
            mongoClient = new MongoClient(uri);
            MongoDatabase database = mongoClient.getDatabase(DBname.getValue().toString());

            // Select operation based on query input
            String queryInput = query.getValue().toString();

            // Check for unsupported delete operations
            if (queryInput.contains("deleteOne") || queryInput.contains("deleteMany") || queryInput.contains("createIndex") || queryInput.contains("dropIndex") || queryInput.contains("getIndexes")) {
                setErrorMessage("Operation not supported.");
                return Result.FAILED;
            }

            MongoOperation operation = MongoOperationFactory.getOperation(queryInput);

            if (operation != null) {
                result = operation.execute(database, queryInput, sb, logger);
                logger.info(queryInput + " : Query executed");

                // Extract only the JSON array part from the result
                String resultString = sb.toString();
                int startIndex = resultString.indexOf("[{");
                int endIndex = resultString.lastIndexOf("}]") + 2;

                // Extract meaningful JSON result
                if (startIndex >= 0 && endIndex > startIndex) {
                    resultString = resultString.substring(startIndex, endIndex);
                }

                // Check if the result is meaningful (e.g., avoid storing irrelevant messages like 'No documents were modified')
                if (resultString.contains("No documents were modified") || resultString.isEmpty()) {
                    // If result is not meaningful, do not store it
                    setErrorMessage("Query executed but no documents were modified or result is empty.");
                    result = Result.FAILED;
                } else {
                    // Store meaningful result in runtime variable
                    runTimeData.setKey(runtimeVar.getValue().toString());
                    runTimeData.setValue(resultString);
                    setSuccessMessage("Query executed successfully. Result stored in variable " + runtimeVar.getValue().toString() + " = " + resultString);
                    logger.info("Value stored in variable: " + runtimeVar.getValue().toString() + " = " + resultString);
                }
            } else {
                setErrorMessage("Unsupported query operation. Only 'find', 'insert', 'update', 'aggregate' and 'index' queries are supported. Query: " + queryInput);
                result = Result.FAILED;
            }
        } catch (Exception e) {
            // Catching and handling exceptions
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = Result.FAILED;
            setErrorMessage(errorMessage);
        } finally {
            // Closing the MongoDB client
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

        return result;
    }
}
