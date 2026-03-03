package com.testsigma.addons.Operations;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;
import com.testsigma.addons.Utils.MongoOperation;
import com.testsigma.sdk.Result;
import org.bson.Document;
import com.testsigma.sdk.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AggregationOperation implements MongoOperation {

    @Override
    public Result execute(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        try {
            logger.info("Received aggregation query: " + queryInput);

            // Extract collection name and aggregation stages
            String collectionName;
            String pipelineJson;

            // Parse the query to extract collection name and aggregation pipeline
            if (queryInput.startsWith("db.")) {
                int startIndex = queryInput.indexOf('.') + 1;
                int endIndex = queryInput.indexOf('.', startIndex);
                collectionName = queryInput.substring(startIndex, endIndex);

                pipelineJson = queryInput.substring(queryInput.indexOf("[") + 1, queryInput.lastIndexOf("]"));
            } else {
                String errorMsg = "Error: Query format is invalid. Expected format: db.collection.aggregate([pipeline])";
                sb.append(errorMsg);
                logger.warn("Invalid query format: " + queryInput);
                logger.warn(errorMsg);
                return Result.FAILED;
            }

            logger.info("Collection name: " + collectionName);
            logger.debug("Pipeline JSON: " + pipelineJson);

            // Parse pipeline JSON into a list of BSON Documents
            String[] stages = pipelineJson.split("},\\s*\\{");
            List<Document> pipeline = new ArrayList<>();
            for (String stage : stages) {
                if (!stage.startsWith("{")) {
                    stage = "{" + stage;
                }
                if (!stage.endsWith("}")) {
                    stage = stage + "}";
                }
                Document parsedStage = Document.parse(stage);
                pipeline.add(parsedStage);
                logger.debug("Added pipeline stage: " + parsedStage.toJson());
            }

            logger.debug("Complete pipeline structure: " + pipeline);

            // Get the collection and execute the aggregation
            MongoCollection<Document> collection = database.getCollection(collectionName);
            logger.info("Executing aggregation on collection: " + collectionName);
            AggregateIterable<Document> results = collection.aggregate(pipeline);

            // Create a JSON array to store results
            JSONArray resultArray = new JSONArray();

            for (Document result : results) {
                // Convert each result to a JSON object
                JSONObject jsonResult = new JSONObject(result.toJson());
                resultArray.put(jsonResult);
            }

            // Append the results to the StringBuffer in compact format (no extra spaces)
            sb.append("Aggregation Results: \n");
            sb.append(resultArray.toString());  // Compact format (no indentation)

            // Log the results
            logger.info("Aggregation completed successfully:");
            logger.info("- Collection: " + collectionName);
            logger.info("- Number of results: " + resultArray.length());
            logger.debug("Results: " + resultArray.toString());

            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage();
            sb.append(errorMsg);
            logger.warn("Error during aggregation execution: " + e.getMessage());
            logger.warn("Stack trace: " + e);
            return Result.FAILED;
        }
    }
}
