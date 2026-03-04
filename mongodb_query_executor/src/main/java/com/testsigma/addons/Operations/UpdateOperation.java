package com.testsigma.addons.Operations;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.testsigma.addons.Utils.MongoOperation;
import com.testsigma.sdk.Result;
import org.bson.Document;
import com.testsigma.sdk.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateOperation implements MongoOperation {

    @Override
    public Result execute(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        try {
            logger.info("Received query: " + queryInput);

            // Regular expressions to identify updateOne and updateMany queries
            Pattern updateOnePattern = Pattern.compile("^db\\.(\\w+)\\.updateOne\\(");
            Pattern updateManyPattern = Pattern.compile("^db\\.(\\w+)\\.updateMany\\(");

            // Match the query to identify if it's updateOne or updateMany
            Matcher matcherOne = updateOnePattern.matcher(queryInput);
            Matcher matcherMany = updateManyPattern.matcher(queryInput);

            String collectionName;
            boolean isUpdateMany = false;

            // Determine collection name and query type (updateOne or updateMany)
            if (matcherOne.find()) {
                collectionName = matcherOne.group(1);
                logger.info("Detected updateOne operation for collection: " + collectionName);
            } else if (matcherMany.find()) {
                collectionName = matcherMany.group(1);
                isUpdateMany = true;
                logger.info("Detected updateMany operation for collection: " + collectionName);
            } else {
                sb.append("Error: Collection name could not be parsed from the query.");
                logger.warn("Failed to detect query type (updateOne or updateMany).");
                return Result.FAILED;
            }

            // Get the collection from the database
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Extract the filter and update parts of the query
            String jsonUpdate = queryInput.substring(queryInput.indexOf("(") + 1, queryInput.lastIndexOf(")"));
            logger.debug("Extracted and formatted JSON: " + jsonUpdate);

            // Split the query into filter and update parts
            String[] parts = jsonUpdate.split(",", 2); // Split on the first comma
            if (parts.length != 2) {
                sb.append("Error: Invalid query format: Missing filter or update section.");
                logger.warn("Failed to split filter and update sections.");
                return Result.FAILED;
            }

            // Parse filter and update documents directly from the extracted query
            Document filter = Document.parse(parts[0].trim());
            Document update = Document.parse(parts[1].trim());

            // Perform the update operation
            if (isUpdateMany) {
                var result = collection.updateMany(filter, update);
                logger.info("Successfully updated " + result.getModifiedCount() + " document(s) in collection " + collectionName);

                if (result.getModifiedCount() > 0) {
                    // Create a filter to fetch the updated documents
                    List<Document> updatedDocuments = fetchUpdatedDocuments(collection, filter, update, true, logger);
                    sb.append(formatDocumentsOutput(updatedDocuments));
                } else {
                    sb.append("No documents were modified in the updateMany operation.");
                }
            } else {
                var result = collection.updateOne(filter, update);
                logger.info("Successfully updated 1 document in collection " + collectionName);

                if (result.getModifiedCount() > 0) {
                    // Create a filter to fetch the updated document
                    List<Document> updatedDocuments = fetchUpdatedDocuments(collection, filter, update, false, logger);
                    sb.append(formatDocumentsOutput(updatedDocuments));
                } else {
                    sb.append("No documents were modified in the updateOne operation.");
                }
            }

            return Result.SUCCESS;
        } catch (Exception e) {
            sb.append("Error: " + e.getMessage());
            logger.warn("Error during query execution: " + e.getMessage());
            return Result.FAILED;
        }
    }


    private List<Document> fetchUpdatedDocuments(MongoCollection<Document> collection, Document filter, Document update, boolean isUpdateMany, Logger logger) {
        // Create a filter that matches the updated document(s)
        Document updatedFilter = new Document();
        if (update.containsKey("$set")) {
            Document setDoc = update.get("$set", Document.class);
            setDoc.forEach((key, value) -> {
                updatedFilter.append(key, value); // Override updated fields
            });
        }

        // Combine with original filter fields that are not modified
        filter.forEach((key, value) -> {
            if (!updatedFilter.containsKey(key)) {
                updatedFilter.append(key, value);
            }
        });

        logger.debug("Updated filter for fetching documents: " + updatedFilter.toJson());

        // Fetch the updated documents
        return isUpdateMany
                ? collection.find(updatedFilter).into(new ArrayList<>())
                : List.of(collection.find(updatedFilter).first());
    }

    private String formatDocumentsOutput(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "[]";
        }

        StringBuilder output = new StringBuilder("[");
        for (int i = 0; i < documents.size(); i++) {
            output.append(documents.get(i).toJson());
            if (i < documents.size() - 1) {
                output.append(", ");
            }
        }
        output.append("]");
        return output.toString();
    }
}
