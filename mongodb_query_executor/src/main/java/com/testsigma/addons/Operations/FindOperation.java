package com.testsigma.addons.Operations;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.testsigma.addons.Utils.MongoOperation;
import org.bson.Document;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindOperation implements MongoOperation {

    @Override
    public Result execute(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger) {
        try {
            logger.info("Received query: " + queryInput);

            // Clean up the query input - remove trailing semicolon if present
            queryInput = queryInput.trim();
            if (queryInput.endsWith(";")) {
                queryInput = queryInput.substring(0, queryInput.length() - 1);
            }

            // Regular expression to identify find queries with more complex patterns
            Pattern findPattern = Pattern.compile("^db\\.(\\w+)\\.find\\((.*)\\)(\\s*\\.\\s*\\w+\\([^)]*\\))*$");
            Matcher findMatcher = findPattern.matcher(queryInput);

            if (!findMatcher.find()) {
                sb.append("Error: Invalid query format. Expected db.collection.find()");
                logger.warn("Failed to parse query format");
                return Result.FAILED;
            }

            String collectionName = findMatcher.group(1);
            String queryParams = findMatcher.group(2).trim();
            logger.info("Detected find operation for collection: " + collectionName);
            logger.debug("Query parameters: " + queryParams);

            // Get the collection
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Parse the query parameters
            Document filter = new Document();
            Document projection = new Document();

            // Handle empty find()
            if (!queryParams.isEmpty()) {
                // Count the number of matching braces to properly split complex queries
                int braceCount = 0;
                int bracketCount = 0;
                StringBuilder currentPart = new StringBuilder();
                List<String> parts = new ArrayList<>();

                for (char c : queryParams.toCharArray()) {
                    currentPart.append(c);
                    switch (c) {
                        case '{':
                            braceCount++;
                            break;
                        case '}':
                            braceCount--;
                            break;
                        case '[':
                            bracketCount++;
                            break;
                        case ']':
                            bracketCount--;
                            break;
                        case ',':
                            if (braceCount == 0 && bracketCount == 0) {
                                // We found a top-level comma
                                String part = currentPart.substring(0, currentPart.length() - 1).trim();
                                if (!part.isEmpty()) {
                                    parts.add(part);
                                }
                                currentPart = new StringBuilder();
                            }
                            break;
                    }
                }

                // Add the last part
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString().trim());
                }

                // Parse filter
                if (!parts.isEmpty()) {
                    String filterStr = parts.get(0);
                    try {
                        filter = Document.parse(filterStr);
                        logger.debug("Parsed filter: " + filter.toJson());
                    } catch (Exception e) {
                        logger.warn("Error parsing filter: " + e.getMessage());
                        throw new RuntimeException("Invalid filter format: " + e.getMessage());
                    }
                }

                // Parse projection if it exists
                if (parts.size() > 1) {
                    try {
                        projection = Document.parse(parts.get(1));
                        logger.debug("Parsed projection: " + projection.toJson());
                    } catch (Exception e) {
                        logger.warn("Error parsing projection: " + e.getMessage());
                        throw new RuntimeException("Invalid projection format: " + e.getMessage());
                    }
                }
            }

            // Handle additional options
            int limit = 0;
            int skip = 0;
            Document sort = new Document();

            // Parse limit
            Pattern limitPattern = Pattern.compile("\\.limit\\((\\d+)\\)");
            Matcher limitMatcher = limitPattern.matcher(queryInput);
            if (limitMatcher.find()) {
                limit = Integer.parseInt(limitMatcher.group(1));
                logger.info("Detected limit: " + limit);
            }

            // Parse skip
            Pattern skipPattern = Pattern.compile("\\.skip\\((\\d+)\\)");
            Matcher skipMatcher = skipPattern.matcher(queryInput);
            if (skipMatcher.find()) {
                skip = Integer.parseInt(skipMatcher.group(1));
                logger.info("Detected skip: " + skip);
            }

            // Parse sort
            Pattern sortPattern = Pattern.compile("\\.sort\\((\\{.*?\\})\\)");
            Matcher sortMatcher = sortPattern.matcher(queryInput);
            if (sortMatcher.find()) {
                sort = Document.parse(sortMatcher.group(1));
                logger.info("Detected sort: " + sort.toJson());
            }

            // Build aggregation pipeline
            List<Document> pipeline = new ArrayList<>();
            pipeline.add(new Document("$match", filter));

            if (!projection.isEmpty()) {
                pipeline.add(new Document("$project", projection));
            }
            if (!sort.isEmpty()) {
                pipeline.add(new Document("$sort", sort));
            }
            if (skip > 0) {
                pipeline.add(new Document("$skip", skip));
            }
            if (limit > 0) {
                pipeline.add(new Document("$limit", limit));
            }

            // Execute query
            ArrayList<Document> results = new ArrayList<>();
            collection.aggregate(pipeline).into(results);

            // Format results and include the values in the success message
            if (results.isEmpty()) {
                sb.append("No documents found matching the filter in collection ").append(collectionName).append(".");
                logger.info("No documents found for query in collection: " + collectionName);
            } else {
                sb.append("Found ").append(results.size()).append(" document(s) in collection ").append(collectionName).append(": [");

                // Append documents in the required format
                for (int i = 0; i < results.size(); i++) {
                    Document doc = results.get(i);
                    sb.append(doc.toJson());
                    if (i < results.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");

                logger.info("Found " + results.size() + " document(s) in collection: " + collectionName);
                for (Document doc : results) {
                    logger.debug("Document found: " + doc.toJson());
                }
            }

            return Result.SUCCESS;

        } catch (Exception e) {
            sb.append("Error: ").append(e.getMessage());
            logger.warn("Error during query execution: " + e.getMessage());
            return Result.FAILED;
        }
    }
}

