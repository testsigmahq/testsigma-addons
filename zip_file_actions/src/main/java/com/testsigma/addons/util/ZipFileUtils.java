package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileUtils {

    private static final int BUFFER_SIZE = 8192;

    /**
     * Single entry-point that validates inputs, resolves the zip source,
     * extracts, finds the target file, and checks for the search text.
     */
    public static VerificationResult verifyTextInZipFile(
            String zipPath, String targetFileName, String searchText, Logger logger) {

        VerificationResult validation = validateInputs(searchText, targetFileName, zipPath, logger);
        if (validation != null) {
            return validation;
        }

        logger.info("Zip file path / URL : " + zipPath);
        logger.info("Target file name    : " + targetFileName);
        logger.info("Search text         : " + searchText);

        return extractAndVerify(zipPath, targetFileName, searchText, logger);
    }

    public static VerificationResult validateInputs(
            String searchText, String targetFileName, String zipPath, Logger logger) {
        if (searchText == null || searchText.isEmpty()) {
            String msg = "Search text (text-to-verify) cannot be empty.";
            logger.info("Validation failed: search text is empty");
            return VerificationResult.failure(msg);
        }
        if (targetFileName == null || targetFileName.isEmpty()) {
            String msg = "File name (file-name) cannot be empty.";
            logger.info("Validation failed: file name is empty");
            return VerificationResult.failure(msg);
        }
        if (zipPath == null || zipPath.isEmpty()) {
            String msg = "Zip file path (zip-file-path) cannot be empty.";
            logger.info("Validation failed: zip file path is empty");
            return VerificationResult.failure(msg);
        }
        return null;
    }

    private static VerificationResult extractAndVerify(
            String zipPath, String targetFileName, String searchText, Logger logger) {
        try {
            boolean isRemote = zipPath.startsWith("https://") || zipPath.startsWith("http://");
            logger.info("Step 1/4: Resolving zip file source"
                    + (isRemote ? " (remote URL detected, downloading...)" : " (local path)"));
            File zipFile = resolveZipFile(zipPath);
            logger.info("Zip file resolved to local path: " + zipFile.getAbsolutePath()
                    + " (size: " + zipFile.length() + " bytes)");

            logger.info("Step 2/4: Extracting zip file...");
            Path extractDir = extractZip(zipFile);
            logger.info("Zip extracted successfully to: " + extractDir);

            logger.info("Step 3/4: Searching for file matching '" + targetFileName + "' in extracted contents...");
            FileSearchResult searchResult = findFile(extractDir, targetFileName);
            logger.info("Total files found in zip: " + searchResult.getAllFiles().size());
            logger.info("Files in zip: " + searchResult.getAllFiles());

            if (!searchResult.isFound()) {
                String errorMsg = String.format(
                        "File with name or prefix '%s' was not found in the extracted zip. "
                                + "Total files extracted: %d. Available files: %s",
                        targetFileName, searchResult.getAllFiles().size(), searchResult.getAllFiles());
                logger.info(errorMsg);
                return VerificationResult.failure(errorMsg);
            }

            Path targetFile = searchResult.getMatchedFile();
            if (searchResult.isExactMatch()) {
                logger.info("Exact match found: " + targetFile.getFileName());
            } else {
                logger.info("No exact match for '" + targetFileName
                        + "'. Prefix match found: " + targetFile.getFileName());
            }
            logger.info("Full path of matched file: " + targetFile.toAbsolutePath());

            logger.info("Step 4/4: Reading file and verifying text presence...");
            String fileContent = readFileContent(targetFile);
            logger.info("File '" + targetFile.getFileName() + "' read successfully "
                    + "(size: " + fileContent.length() + " characters)");

            if (containsText(fileContent, searchText)) {
                String successMsg = String.format(
                        "Successfully verified that the given text is present in the file '%s'",
                        targetFile.getFileName());
                logger.info("Verification PASSED — " + successMsg);
                return VerificationResult.success(successMsg);
            } else {
                String errorMsg = String.format(
                        "Given text was NOT found in the file '%s'",
                        targetFile.getFileName()); 
                logger.info("Verification FAILED — " + errorMsg);
                return VerificationResult.failure(errorMsg);
            }

        } catch (FileNotFoundException e) {
            String errorMsg = "File not found: " + e.getMessage();
            logger.info(errorMsg);
            return VerificationResult.failure(errorMsg);
        } catch (IOException e) {
            String errorMsg = "I/O error during zip extraction or file reading: " + e.getMessage();
            logger.info("IOException: " + e.getMessage());
            logger.debug("IOException stacktrace: " + e);
            return VerificationResult.failure(errorMsg);
        } catch (IllegalArgumentException e) {
            logger.info("Validation error: " + e.getMessage());
            return VerificationResult.failure(e.getMessage());
        } catch (Exception e) {
            String errorMsg = "Unexpected error while verifying text in zip file: " + e.getMessage();
            logger.info("Unexpected error: " + e.getMessage());
            logger.debug("Unexpected exception stacktrace: " + e);
            return VerificationResult.failure(errorMsg);
        }
    }

    // ─── Low-level helpers (still public for direct use if needed) ───

    public static File resolveZipFile(String zipFilePathOrUrl) throws IOException {
        if (zipFilePathOrUrl.startsWith("https://") || zipFilePathOrUrl.startsWith("http://")) {
            URI uri = URI.create(zipFilePathOrUrl);
            URL url = uri.toURL();
            String urlPath = uri.getPath();
            String baseName = "downloaded_zip";
            String extension = ".zip";

            int lastSlash = urlPath.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < urlPath.length() - 1) {
                String urlFileName = urlPath.substring(lastSlash + 1);
                int dotIndex = urlFileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    baseName = urlFileName.substring(0, dotIndex);
                    extension = urlFileName.substring(dotIndex);
                } else {
                    baseName = urlFileName;
                }
            }

            File tempFile = File.createTempFile(baseName + "_", extension);
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile;
        } else {
            File localFile = new File(zipFilePathOrUrl);
            if (!localFile.exists()) {
                throw new FileNotFoundException(
                        "Zip file not found at local path: " + localFile.getAbsolutePath());
            }
            return localFile;
        }
    }

    public static Path extractZip(File zipFile) throws IOException {
        Path zipPath = zipFile.toPath().toAbsolutePath().normalize();

        if (!Files.exists(zipPath)) {
            throw new FileNotFoundException("Zip file not found at path: " + zipPath);
        }
        if (!zipPath.toString().toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException(
                    "Specified file is not a .zip file: " + zipPath.getFileName());
        }

        String zipName = zipPath.getFileName().toString();
        String dirName = zipName.substring(0, zipName.lastIndexOf('.'));
        Path extractDir = zipPath.getParent().resolve(dirName + "_extracted");
        Files.createDirectories(extractDir);

        int entryCount = 0;
        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipPath.toFile())))) {
            ZipEntry entry;
            byte[] buffer = new byte[BUFFER_SIZE];

            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = extractDir.resolve(entry.getName()).normalize();

                if (!entryPath.startsWith(extractDir)) {
                    throw new IOException(
                            "Invalid zip entry — path traversal detected: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (OutputStream os = new BufferedOutputStream(
                            new FileOutputStream(entryPath.toFile()))) {
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                    entryCount++;
                }
                zis.closeEntry();
            }
        }

        if (entryCount == 0) {
            throw new IOException("Zip file appears to be empty — no file entries found in: "
                    + zipPath.getFileName());
        }

        return extractDir;
    }

    public static FileSearchResult findFile(Path directory, String fileName) throws IOException {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new FileNotFoundException("Extraction directory not found: " + directory);
        }

        List<Path> allFiles;
        try (Stream<Path> paths = Files.walk(directory)) {
            allFiles = paths
                    .filter(p -> !Files.isDirectory(p))
                    .collect(Collectors.toList());
        }

        List<String> allRelativePaths = allFiles.stream()
                .map(p -> directory.relativize(p).toString())
                .sorted()
                .collect(Collectors.toList());

        Path exactMatch = allFiles.stream()
                .filter(p -> p.getFileName().toString().equals(fileName))
                .findFirst()
                .orElse(null);

        if (exactMatch != null) {
            return new FileSearchResult(exactMatch, true, allRelativePaths);
        }

        Path prefixMatch = allFiles.stream()
                .filter(p -> p.getFileName().toString().startsWith(fileName))
                .findFirst()
                .orElse(null);

        if (prefixMatch != null) {
            return new FileSearchResult(prefixMatch, false, allRelativePaths);
        }

        return new FileSearchResult(null, false, allRelativePaths);
    }

    public static String readFileContent(Path filePath) throws IOException {
        return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
    }

    public static boolean containsText(String content, String searchText) {
        if (content == null || searchText == null) {
            return false;
        }
        return content.contains(searchText);
    }

    // ─── Result classes ───

    public static class VerificationResult {
        private final boolean success;
        private final String message;

        private VerificationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static VerificationResult success(String message) {
            return new VerificationResult(true, message);
        }

        public static VerificationResult failure(String message) {
            return new VerificationResult(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public static class FileSearchResult {
        private final Path matchedFile;
        private final boolean exactMatch;
        private final List<String> allFiles;

        public FileSearchResult(Path matchedFile, boolean exactMatch, List<String> allFiles) {
            this.matchedFile = matchedFile;
            this.exactMatch = exactMatch;
            this.allFiles = allFiles;
        }

        public Path getMatchedFile() { return matchedFile; }
        public boolean isExactMatch() { return exactMatch; }
        public List<String> getAllFiles() { return allFiles; }
        public boolean isFound() { return matchedFile != null; }
    }
}
