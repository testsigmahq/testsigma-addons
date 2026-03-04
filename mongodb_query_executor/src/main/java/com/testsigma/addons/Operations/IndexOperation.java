package com.testsigma.addons.Operations;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.MongoIterable;
import com.testsigma.addons.Utils.MongoOperation;
import com.testsigma.sdk.Result;
import org.bson.Document;
import com.testsigma.sdk.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndexOperation implements MongoOperation {

    @Override
    public Result execute(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        try {
            // Handle different types of index operations dynamically
            if (queryInput.contains(".createIndex(")) {
                return handleCreateIndex(database, queryInput, sb, logger);
            } else if (queryInput.contains(".dropIndex(")) {
                return handleDropIndex(database, queryInput, sb, logger);
            } else if (queryInput.contains(".getIndexes(")) {
                return handleGetIndexes(database, queryInput, sb, logger);
            } else {
                logger.warn("Unsupported index operation.");
                sb.append("Error: Unsupported index operation.\n");
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.warn("Error executing index operation: " + e.getMessage());
            sb.append("Error: " + e.getMessage() + "\n");
            return Result.FAILED;
        }
    }

    // Handling index creation
    private Result handleCreateIndex(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        String collectionName = extractCollectionName(queryInput);
        Document index = extractIndex(queryInput);  // Dynamic index based on the query input
        IndexOptions options = new IndexOptions().unique(true);  // Enforcing unique index constraint
        MongoCollection<Document> collection = database.getCollection(collectionName);

        try {
            String indexName = collection.createIndex(index, options);
            sb.append("Success: Index created on collection '").append(collectionName)
                    .append("' with index name '").append(indexName).append("'.\n");
            logger.info("Successfully created index: " + indexName);
            return Result.SUCCESS;
        } catch (Exception e) {
            // Handle duplicate key error (error code 11000)
            if (e.getMessage().contains("E11000")) {
                sb.append("Error: Duplicate key error during index creation. Please check for duplicate values in the indexed fields.\n");
                logger.warn("Duplicate key error during index creation: " + e.getMessage());
            } else {
                sb.append("Error: " + e.getMessage() + "\n");
                logger.warn("Error during index creation: " + e.getMessage());
            }
            return Result.FAILED;
        }
    }

    // Handling index drop
    private Result handleDropIndex(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        String collectionName = extractCollectionName(queryInput);
        String indexName = extractIndexName(queryInput);  // Extract dynamic index name

        MongoCollection<Document> collection = database.getCollection(collectionName);
        try {
            collection.dropIndex(indexName);
            sb.append("Success: Index '").append(indexName).append("' dropped from collection '")
                    .append(collectionName).append("'.\n");
            logger.info("Successfully dropped index: " + indexName);
            return Result.SUCCESS;
        } catch (Exception e) {
            sb.append("Error: " + e.getMessage() + "\n");
            logger.warn("Error during index drop: " + e.getMessage());
            return Result.FAILED;
        }
    }

    // Handling fetching index details
    private Result handleGetIndexes(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        String collectionName = extractCollectionName(queryInput);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        try {
            MongoIterable<Document> indexes = collection.listIndexes();
            sb.append("Success: Retrieved indexes for collection '").append(collectionName).append("':\n");
            for (Document index : indexes) {
                sb.append(index.toJson()).append("\n");
                logger.info("Index: " + index.toJson());
            }
            return Result.SUCCESS;
        } catch (Exception e) {
            sb.append("Error: " + e.getMessage() + "\n");
            logger.warn("Error fetching indexes: " + e.getMessage());
            return Result.FAILED;
        }
    }

    // Utility method to dynamically extract collection name from the query input
    private String extractCollectionName(String queryInput) {
        Pattern pattern = Pattern.compile("db\\.(\\w+)\\.", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(queryInput);
        if (matcher.find()) {
            return matcher.group(1);  // Extracts the collection name
        } else {
            throw new IllegalArgumentException("Invalid query format: Could not extract collection name.");
        }
    }

    // Utility method to dynamically extract the index name from the query input (for dropIndex)
    private String extractIndexName(String queryInput) {
        // First check if it's an index name string, e.g., 'age_1_email_1'
        Pattern namePattern = Pattern.compile("dropIndex\\(['\"](.*?)['\"]\\)", Pattern.CASE_INSENSITIVE);
        Matcher nameMatcher = namePattern.matcher(queryInput);
        if (nameMatcher.find()) {
            return nameMatcher.group(1);  // Extracts the index name (string)
        }

        // Otherwise, try to handle index specification (document), e.g., { age: 1, email: 1 }
        Pattern docPattern = Pattern.compile("dropIndex\\((\\{.*?\\})\\)", Pattern.CASE_INSENSITIVE);
        Matcher docMatcher = docPattern.matcher(queryInput);
        if (docMatcher.find()) {
            String indexJson = docMatcher.group(1);  // Extract the index specification as a JSON string
            return Document.parse(indexJson).toJson();  // Return the JSON as string for logging, etc.
        }

        throw new IllegalArgumentException("Invalid query format: Could not extract index name or specification.");
    }

    // Utility method to dynamically extract the index fields for createIndex
    private Document extractIndex(String queryInput) {
        // Example: extract index from a query like db.collection.createIndex({"name": 1})
        Pattern pattern = Pattern.compile("createIndex\\((\\{.*?\\})\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(queryInput);
        if (matcher.find()) {
            String indexJson = matcher.group(1);  // Extract the JSON string for the index
            return Document.parse(indexJson);  // Convert it to a BSON Document
        } else {
            throw new IllegalArgumentException("Invalid query format: Could not extract index fields.");
        }
    }
}
