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

public class DeleteOperation implements MongoOperation {

    @Override
    public Result execute(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        try {
            logger.info("Received query: " + queryInput);

            // Regular expressions to identify deleteOne and deleteMany queries
            Pattern deleteOnePattern = Pattern.compile("^db\\.(\\w+)\\.deleteOne\\(");
            Pattern deleteManyPattern = Pattern.compile("^db\\.(\\w+)\\.deleteMany\\(");

            // Match the query to identify if it's deleteOne or deleteMany
            Matcher matcherOne = deleteOnePattern.matcher(queryInput);
            Matcher matcherMany = deleteManyPattern.matcher(queryInput);

            String collectionName;
            boolean isDeleteMany = false;

            // Determine collection name and query type (deleteOne or deleteMany)
            if (matcherOne.find()) {
                collectionName = matcherOne.group(1);
                logger.info("Detected deleteOne operation for collection: " + collectionName);
            } else if (matcherMany.find()) {
                collectionName = matcherMany.group(1);
                isDeleteMany = true;
                logger.info("Detected deleteMany operation for collection: " + collectionName);
            } else {
                sb.append("Error: Collection name could not be parsed from the query.");
                logger.warn("Failed to detect query type (deleteOne or deleteMany).");
                return Result.FAILED;
            }

            // Get the collection from the database
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Extract JSON query portion from the input
            String jsonQuery = queryInput.substring(queryInput.indexOf("(") + 1, queryInput.lastIndexOf(")"));
            logger.debug("Extracted and formatted JSON: " + jsonQuery);

            // Parse the filter from the query
            Document filter = Document.parse(jsonQuery);

            // StringBuilder to accumulate deleted documents
            StringBuilder deletedDocumentsLog = new StringBuilder();

            // If it's a deleteMany operation
            if (isDeleteMany) {
                // Fetch documents that will be deleted
                List<Document> documentsBeforeDelete = collection.find(filter).into(new ArrayList<>());
                long deletedCount = collection.deleteMany(filter).getDeletedCount();

                if (deletedCount > 0) {
                    sb.append("Successfully deleted " + deletedCount + " document(s) in collection " + collectionName + ".\n");
                    for (Document doc : documentsBeforeDelete) {
                        deletedDocumentsLog.append("Deleted Document: " + doc.toJson() + "\n");
                    }
                    sb.append(deletedDocumentsLog.toString());  // Append the deleted documents to the response
                    logger.info("Successfully deleted " + deletedCount + " document(s) in collection " + collectionName);
                    logger.info("Deleted Documents: \n" + deletedDocumentsLog.toString());  // Log all deleted documents at once
                } else {
                    sb.append("No documents found matching the filter in collection " + collectionName + ".");
                    logger.info("No documents found matching the filter in collection " + collectionName);
                }
            } else {
                // Perform deleteOne operation
                Document documentBeforeDelete = collection.find(filter).first();
                long deletedCount = collection.deleteOne(filter).getDeletedCount();

                if (deletedCount > 0 && documentBeforeDelete != null) {
                    sb.append("Successfully deleted 1 document in collection " + collectionName + ".\n");
                    sb.append("Deleted Document: " + documentBeforeDelete.toJson() + "\n");
                    deletedDocumentsLog.append("Deleted Document: " + documentBeforeDelete.toJson() + "\n");
                    logger.info("Successfully deleted 1 document in collection " + collectionName);
                    logger.info("Deleted Documents: \n" + deletedDocumentsLog.toString());  // Log the deleted document at once
                } else {
                    sb.append("No document found matching the filter in collection " + collectionName + ".");
                    logger.info("No document found matching the filter in collection " + collectionName);
                }
            }

            return Result.SUCCESS;
        } catch (Exception e) {
            sb.append("Error: " + e.getMessage());
            logger.warn("Error during query execution: " + e.getMessage());
            return Result.FAILED;
        }
    }
}

