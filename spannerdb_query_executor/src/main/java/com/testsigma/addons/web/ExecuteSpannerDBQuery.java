package com.testsigma.addons.web;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.spanner.*;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

@Data
@Action(
        actionText = "Spanner DB: Execute Spanner Query. Project: projectId, Instance: instanceId, Database: databaseId, Query: query. Authenticate using Service Account JSON File Path: serviceAccountKeyPath. Store result in: variable-name",
        description = "Connects to Google Cloud Spanner. Supports SELECT (returns CSV result) and DML (INSERT, UPDATE, DELETE - returns affected row count).",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class ExecuteSpannerDBQuery extends WebAction {

    @TestData(reference = "projectId")
    private com.testsigma.sdk.TestData projectIdData;

    @TestData(reference = "instanceId")
    private com.testsigma.sdk.TestData instanceIdData;

    @TestData(reference = "databaseId")
    private com.testsigma.sdk.TestData databaseIdData;

    @TestData(reference = "query")
    private com.testsigma.sdk.TestData queryData;

    // Service account JSON file path
    @TestData(reference = "serviceAccountKeyPath")
    private com.testsigma.sdk.TestData serviceAccountKeyPathData;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        StringBuilder output = new StringBuilder();

        try {
            // 1. Read inputs
            String projectId = projectIdData.getValue().toString().trim();
            String instanceId = instanceIdData.getValue().toString().trim();
            String databaseId = databaseIdData.getValue().toString().trim();
            String sql = queryData.getValue().toString().trim();
            String keyFilePath = convertToFile(serviceAccountKeyPathData.getValue().toString().trim()).getAbsolutePath();

            logger.info(String.format("Connecting to Spanner | Project=%s, Instance=%s, Database=%s",
                    projectId, instanceId, databaseId));

            // 2. Load credentials from JSON file
            GoogleCredentials credentials =
                    GoogleCredentials.fromStream(new FileInputStream(keyFilePath));

            SpannerOptions options = SpannerOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build();

            // 3. Execute query
            try (Spanner spanner = options.getService()) {
                DatabaseId db = DatabaseId.of(projectId, instanceId, databaseId);
                DatabaseClient client = spanner.getDatabaseClient(db);

                // Check query type
                String upperSql = sql.toUpperCase();

                if (upperSql.startsWith("SELECT") || upperSql.startsWith("WITH") || upperSql.startsWith("GRAPH")) {
                    // --- READ ONLY PATH (SELECT) ---
                    try (ResultSet rs = client.singleUse().executeQuery(Statement.of(sql))) {
                        // Fix: Iterate using next() before accessing getColumnCount()
                        while (rs.next()) {
                            int columnCount = rs.getColumnCount();
                            for (int i = 0; i < columnCount; i++) {
                                // getValue(i) returns generic Value object, toString() provides string representation
                                output.append(rs.getValue(i).toString());
                                if (i < columnCount - 1) {
                                    output.append(", ");
                                }
                            }
                            output.append("\n");
                        }
                    }
                } else {
                    // --- READ/WRITE PATH (INSERT, UPDATE, DELETE) ---
                    // DML statements must be executed within a ReadWriteTransaction
                    Long rowCount = client.readWriteTransaction().run(transaction -> {
                        return transaction.executeUpdate(Statement.of(sql));
                    });
                    output.append(rowCount); // Store the number of affected rows
                    logger.info("DML Executed. Rows affected: " + rowCount);
                }
            }

            // 4. Store result in runtime variable
            if (runTimeData != null) {
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue(output.toString().trim());
            }

            setSuccessMessage("Query executed successfully. Result: " + output.toString().trim());

        } catch (Exception e) {
            logger.warn(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Spanner Error: " + ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }

        return result;
    }

    private File convertToFile(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("https://") || pathOrUrl.startsWith("http://")) {
            // Extract the original file name from the URL
            String originalFileName = FilenameUtils.getName(new URL(pathOrUrl).getPath());
            // Generate a unique file name by appending a timestamp
            String uniqueFileName = "temp_" + System.currentTimeMillis() + "_" + originalFileName;
            logger.info("Given is a URL... Original file name: " + originalFileName + ", Unique file name: "
                    + uniqueFileName);

            // Create the full path for the temporary file
            String filePath = String.format("%s%s%s", FileUtils.getTempDirectoryPath(), File.separator, uniqueFileName);
            File tempFile = new File(filePath);

            // Download the file from the URL to the temporary location
            FileUtils.copyURLToFile(new URL(pathOrUrl), tempFile, 10000, 10000); // Adding timeouts
            logger.info("Temp file created for URL file: " + uniqueFileName + " at path " + filePath);

            return tempFile;
        } else {
            logger.info("Given is a local file path...");
            return new File(pathOrUrl);
        }
    }
}