package com.testsigma.addons.Utils;


import com.testsigma.addons.Operations.*;

public class MongoOperationFactory {

    public static MongoOperation getOperation(String queryInput) {
        // Match the query type based on regex patterns

        // Check if query contains .find( for find operations
        if (queryInput.startsWith("db.") && queryInput.contains(".find(")) {
            return new FindOperation(); // For find queries

            // Check for aggregation queries (aggregate())
        } else if (queryInput.startsWith("db.") && queryInput.contains(".aggregate([")) {
            return new AggregationOperation(); // For aggregation queries

            // Check for insert queries (insertOne, insertMany)
        } else if (queryInput.startsWith("db.") && (queryInput.contains(".insertOne(") || queryInput.contains(".insertMany("))) {
            return new InsertOperation(); // For both insert queries

            // Check for update queries (updateOne, updateMany)
        } else if (queryInput.startsWith("db.") && (queryInput.contains(".updateOne(") || queryInput.contains(".updateMany("))) {
            return new UpdateOperation(); // For update queries

            // Check for delete queries (deleteOne, deleteMany)
        } else if (queryInput.startsWith("db.") && (queryInput.contains(".deleteOne(") || queryInput.contains(".deleteMany("))) {
            return new DeleteOperation(); // For delete queries

            // Check for index-related queries (createIndex, dropIndex, getIndexes)
        } else if (queryInput.startsWith("db.") && (
                queryInput.contains(".createIndex(") ||
                        queryInput.contains(".dropIndex(") ||
                        queryInput.contains(".getIndexes("))) {
            return new IndexOperation(); // For index-related queries (create, drop, list)

            // Unsupported query type
        } else {
            return null;
        }
    }
}
