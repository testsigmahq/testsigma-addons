package com.testsigma.addons.pdf_action_with_file_input;

import com.testsigma.sdk.WebAction;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Base class for PDF actions that take a file path (local or URL) as testdata.
 * Provides urlToFileConverter and PDF type verification.
 */
public abstract class PdfFileInputBase extends WebAction {

    private static final String PDF_MAGIC = "%PDF";
    private static final String NOT_PDF_MESSAGE = "The given file is not a valid PDF. Please provide a PDF file path or URL.";

    /**
     * Converts a URL or local file path to a File. Supports https/http URLs (e.g. S3) and local paths.
     */
    public File urlToFileConverter(String fileName, String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Given is s3 url ...File name:" + fileName);
                URL urlObject = new URL(url);
                int lastDot = fileName.lastIndexOf('.');
                String prefix = lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
                String suffix = lastDot > 0 ? fileName.substring(lastDot) : ".pdf";
                File tempFile = File.createTempFile(prefix, suffix);
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created with name for s3 file" + tempFile.getName() + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                return new File(url);
            }
        } catch (Exception e) {
            setErrorMessage("Unable to access the given pdfs, please check the given inputs.");
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access pdfs");
        }
    }

    /**
     * Verifies that the file exists and is a PDF (by extension and magic bytes).
     * Sets error message and returns false if not valid; returns true if valid.
     */
    public boolean verifyPdfFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            setErrorMessage("File does not exist or is not a valid file: " + (file != null ? file.getAbsolutePath() : "null"));
            return false;
        }
        String name = file.getName();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            setErrorMessage(NOT_PDF_MESSAGE + " File: " + file.getAbsolutePath());
            return false;
        }
        try {
            byte[] header = new byte[5];
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                if (fis.read(header) < 5) {
                    setErrorMessage(NOT_PDF_MESSAGE + " File: " + file.getAbsolutePath());
                    return false;
                }
            }
            String headerStr = new String(header, java.nio.charset.StandardCharsets.US_ASCII);
            if (!headerStr.startsWith(PDF_MAGIC)) {
                setErrorMessage(NOT_PDF_MESSAGE + " File: " + file.getAbsolutePath());
                return false;
            }
        } catch (IOException e) {
            setErrorMessage("Unable to read file: " + file.getAbsolutePath() + " - " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Loads a PDDocument from the given File. Caller must close the document.
     */
    protected PDDocument loadPdfDocument(File file) throws IOException {
        return Loader.loadPDF(file);
    }
}
