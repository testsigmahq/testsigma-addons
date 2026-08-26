package com.testsigma.addons.web.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public final class FileDownloadUtil {

    private static final Random FILENAME_RANDOM = new Random();

    private FileDownloadUtil() {
    }

    /**
     * Downloads a remote file to the system temp directory using a short basename (≤63 chars)
     * derived from the URL path (percent-decoded), for UIs that limit filename length.
     */
    public static File downloadUrlToTempFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String rawName = Paths.get(url.getPath()).getFileName().toString();
        String fileName;
        try {
            fileName = URLDecoder.decode(rawName, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            fileName = rawName;
        }
        String ext = tempSuffixFromFileName(fileName);
        String stem = fileStem(fileName);
        stem = sanitizeTempStem(stem);
        File tempFile = createShortNamedTempFile(stem, ext);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private static File createShortNamedTempFile(String stem, String ext) throws IOException {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"));
        final int maxBase = 63;
        final int uniqueLen = 8;
        int maxStem = maxBase - ext.length() - 1 - uniqueLen;
        if (maxStem < 1) {
            maxStem = 1;
        }
        if (stem.length() > maxStem) {
            stem = stem.substring(0, maxStem);
        }
        for (int attempt = 0; attempt < 64; attempt++) {
            String unique = String.format("%08x", FILENAME_RANDOM.nextInt());
            String name = stem + "-" + unique + ext;
            Path path = dir.resolve(name);
            try {
                Files.createFile(path);
                return path.toFile();
            } catch (FileAlreadyExistsException ignored) {
                // retry
            }
        }
        return File.createTempFile("ex-", ext);
    }

    private static String fileStem(String name) {
        int dot = name.lastIndexOf('.');
        if (dot <= 0) {
            return name.isEmpty() ? "excel" : name;
        }
        return name.substring(0, dot);
    }

    private static String sanitizeTempStem(String stem) {
        if (stem == null || stem.isEmpty()) {
            return "excel";
        }
        String s = stem.replaceAll("[^a-zA-Z0-9._()-]", "_");
        s = s.replaceAll("_+", "_");
        if (s.isEmpty() || ".".equals(s)) {
            return "excel";
        }
        return s;
    }

    private static String tempSuffixFromFileName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return ".tmp";
        }
        String ext = fileName.substring(dot);
        if (ext.length() > 12) {
            return ".tmp";
        }
        return ext;
    }

    public static File csvFilePath(String filePath) throws IOException {
        File csvFilePath;

        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            csvFilePath = downloadFile(filePath);
        } else {
            csvFilePath = new File(filePath);
        }

        return csvFilePath;
    }

    public static File downloadFile(String fileUrl) throws IOException {
        return downloadUrlToTempFile(fileUrl);
    }

    public static boolean isUrl(String path) {
        return path != null && (path.startsWith("http://") || path.startsWith("https://"));
    }

    /**
     * Resolves a test data value to a local file, supporting three forms: a local directory
     * (the most recently modified file inside it is picked), an http(s) URL (delegated to
     * {@link #csvFilePath}, which downloads it to a temp file), or a local file path (also
     * delegated to {@link #csvFilePath}).
     */
    public static File resolveInputFile(String path, String label) throws InvalidTestDataException {
        if (path == null || path.trim().isEmpty()) {
            throw new InvalidTestDataException(label + " test data value is empty. Please provide a valid "
                    + "directory path, file path, or upload URL.");
        }

        if (!isUrl(path)) {
            File file = new File(path);
            if (!file.exists()) {
                throw new InvalidTestDataException(label + " path does not exist on this machine: " + path);
            }
            if (file.isDirectory()) {
                File latest = getLastModified(path);
                if (latest == null) {
                    throw new InvalidTestDataException("No file found inside " + label + " directory: " + path
                            + ". Make sure the file has been generated/downloaded before this step runs.");
                }
                return latest;
            }
            if (!file.isFile()) {
                throw new InvalidTestDataException(label + " path is neither a file nor a directory: " + path);
            }
        }

        try {
            return csvFilePath(path);
        } catch (IOException e) {
            throw new InvalidTestDataException("Failed to download " + label + " file from URL: " + path
                    + ". " + e.getMessage());
        }
    }

    public static File getLastModified(String directoryFilePath) {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            return null;
        }
        File chosenFile = files[0];
        for (File file : files) {
            if (file.lastModified() > chosenFile.lastModified()
                    || (file.lastModified() == chosenFile.lastModified()
                    && file.getName().compareTo(chosenFile.getName()) > 0)) {
                chosenFile = file;
            }
        }
        return chosenFile;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // no-op
            }
        }
    }

    public static void deleteQuietly(File file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException ignored) {
                // no-op
            }
        }
    }

    public static class InvalidTestDataException extends Exception {
        public InvalidTestDataException(String message) {
            super(message);
        }
    }
}
