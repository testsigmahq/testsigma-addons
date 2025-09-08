package com.testsigma.addons.web.utilities;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileUtilities {

    private static final String folderPath = "/tmp/ntuc_data";

    private static String getFileName(Long runId) {
        return folderPath + "/" + runId;
    }

    public static void writeToFile(Long runId, String content) throws Exception {
        String fileName = getFileName(runId);
        File file = new File(fileName);
        try {
            // Ensure the parent directory exists without overwriting it
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parentDir);
                }
            }
            // Check if file exists, delete if it does
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException("Failed to delete existing file: " + file);
                }
            }
            // Write the content to the file
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            // Write the JSON content to the file
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    public static String readFromFile(Long runId) {
        String fileName = getFileName(runId);
        File file = new File(fileName);
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
