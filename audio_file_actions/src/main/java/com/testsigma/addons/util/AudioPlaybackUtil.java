package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import javazoom.jl.player.Player;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

public class AudioPlaybackUtil {

    private final Logger logger;

    public AudioPlaybackUtil(Logger logger) {
        this.logger = logger;
    }

    /**
     * Extracts just the file name from a URL or local path, stripping any query parameters.
     * e.g. "https://s3.../sample-audio.mp3?X-Amz-..." → "sample-audio.mp3"
     */
    public String extractFileName(String urlOrPath) {
        String path = urlOrPath;
        if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) {
            try {
                path = new URL(urlOrPath).getPath();
            } catch (Exception e) {
                int queryIndex = urlOrPath.indexOf('?');
                if (queryIndex != -1) {
                    path = urlOrPath.substring(0, queryIndex);
                }
            }
        }
        return path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
    }

    /**
     * Validates that the file is an MP3. Throws IllegalArgumentException for any other format.
     */
    public void validateFormat(String fileName) {
        if (!fileName.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException(
                    "Unsupported audio format for file '" + fileName + "'. Only .mp3 format is allowed.");
        }
    }

    /**
     * Resolves a URL or local file path to a File object.
     * For http/https URLs the content is downloaded to a temp file first.
     */
    public File urlToFileConverter(String fileName, String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Given is s3 url ...File name: " + fileName);
                URL urlObject = new URL(url);
                File tempFile = File.createTempFile(fileName.split("\\.")[0],
                        "." + fileName.split("\\.")[1]);
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created: " + tempFile.getName() + " at " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path: " + url);
                return new File(url);
            }
        } catch (Exception e) {
            logger.warn("Error while accessing: " + url);
            logger.info("Exception: " + e.getMessage());
            throw new RuntimeException("Unable to access audio file: " + url, e);
        }
    }

    /**
     * Plays an MP3 file using the JLayer library.
     */
    public void playMp3(File audioFile) throws Exception {
        logger.info("Using JLayer for MP3 playback: " + audioFile.getName());
        try (FileInputStream fis = new FileInputStream(audioFile)) {
            Player player = new Player(fis);
            player.play();
        }
        logger.info("MP3 playback complete");
    }
}
