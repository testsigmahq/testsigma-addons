package com.testsigma.addons.utils;

import com.testsigma.sdk.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileHelper {

    public static File urlToFileConverter(Logger logger, String fileName, String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Given is s3 url ...File name:" + fileName);
                URL urlObject = new URL(url);
                String baseName = fileName;
                String extension = "";
                int lastDotIndex = fileName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    baseName = fileName.substring(0, lastDotIndex);
                    extension = fileName.substring(lastDotIndex);
                }
                File tempFile = File.createTempFile(baseName, extension);
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created with name for s3 file" + tempFile.getName()
                        + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                return new File(url);
            }
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access the given file, please check the given inputs.");
        }
    }

    public static String readFileContent(File file) throws Exception {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    public static boolean isHtmlFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".html") || lower.endsWith(".htm");
    }

    public static String stripHtmlTags(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) return "";
        return htmlContent.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    public static String extractFileName(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) return "file";
        String path = pathOrUrl.contains("?") ? pathOrUrl.substring(0, pathOrUrl.indexOf('?')) : pathOrUrl;
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}
