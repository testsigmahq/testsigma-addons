package com.testsigma.addons.Operations;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.testsigma.addons.Utils.MongoOperation;
import com.testsigma.sdk.Result;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import com.testsigma.sdk.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertOperation implements MongoOperation {

    @Override
    public Result execute(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        try {
            logger.info("[InsertOperation] Starting query execution...");
            logger.info("[InsertOperation] Query Input: " + queryInput);

            // Regular expressions to identify insertOne and insertMany queries
            Pattern insertOnePattern = Pattern.compile("^db\\.(\\w+)\\.insertOne\\(");
            Pattern insertManyPattern = Pattern.compile("^db\\.(\\w+)\\.insertMany\\(");

            // Match the query to identify if it's insertOne or insertMany
            Matcher matcherOne = insertOnePattern.matcher(queryInput);
            Matcher matcherMany = insertManyPattern.matcher(queryInput);

            String collectionName;
            boolean isInsertMany = false;

            // Determine collection name and query type (insertOne or insertMany)
            if (matcherOne.find()) {
                collectionName = matcherOne.group(1);
                logger.info("[InsertOperation] Detected 'insertOne' operation for collection: " + collectionName);
            } else if (matcherMany.find()) {
                collectionName = matcherMany.group(1);
                isInsertMany = true;
                logger.info("[InsertOperation] Detected 'insertMany' operation for collection: " + collectionName);
            } else {
                sb.append("[InsertOperation] Error: Collection name could not be parsed from the query.");
                logger.warn("[InsertOperation] Failed to detect query type (insertOne or insertMany).");
                return Result.FAILED;
            }

            // Get the collection from the database
            MongoCollection<Document> collection = database.getCollection(collectionName);

            if (isInsertMany) {
                // Extract the JSON array part of the query
                String jsonInsert = queryInput.substring(
                        queryInput.indexOf("["),
                        queryInput.lastIndexOf("]") + 1
                );

                // Parse JSON array for insertMany
                JSONArray jsonArray = new JSONArray(jsonInsert);
                List<Document> documentList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Document document = Document.parse(jsonObject.toString());
                    documentList.add(document);
                }

                // Insert documents
                collection.insertMany(documentList);
                sb.append("Documents inserted successfully: ").append(documentList.size());

                logger.info("[InsertOperation] Successfully inserted " + documentList.size() + " documents into collection: " + collectionName);

                // Consolidate logs for all inserted documents into a JSON array format
                StringBuilder consolidatedLogs = new StringBuilder("[InsertOperation] Inserted documents: [");
                for (Document doc : documentList) {
                    consolidatedLogs.append(doc.toJson()).append(", ");
                }

                // Remove the last comma and space, and close the array
                if (consolidatedLogs.length() > 2) {
                    consolidatedLogs.setLength(consolidatedLogs.length() - 2);
                }
                consolidatedLogs.append("]");

                // Log all documents in a single line
                logger.info(consolidatedLogs.toString());
                sb.append("\n").append(consolidatedLogs.toString());
            } else {
                // Extract and parse single JSON object for insertOne
                String jsonObject = queryInput.substring(
                        queryInput.indexOf("{"),
                        queryInput.lastIndexOf("}") + 1
                );
                Document document = Document.parse(jsonObject);
                collection.insertOne(document);

                // Log the insertion details
                logger.info("[InsertOperation] Successfully inserted document into collection: " + collectionName);
                logger.info("[InsertOperation] Document: " + document.toJson());

                // Append the document in the desired format (as a JSON array in a string)
                sb.append("Document inserted successfully: [").append(document.toJson()).append("]");
            }

            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "[InsertOperation] Error during query execution: " + e.getMessage();
            sb.append(errorMessage);
            logger.warn(errorMessage + e);
            return Result.FAILED;
        }
    }
}
