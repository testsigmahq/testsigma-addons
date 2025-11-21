package com.testsigma.addons.web;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

@Data
@Action(actionText = "Extract data from PDF pdf_path using ocr and store it in runtime variable variable-name",
        description = "Extracts text from a PDF file and performs OCR on images embedded in the PDF.",
        applicationType = ApplicationType.WEB)
public class OCRextractFromPDF extends WebAction {

    @TestData(reference = "pdf_path")
    private com.testsigma.sdk.TestData pdfPath;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testdata;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        try {
            String filePath = pdfPath.getValue().toString();
            File pdfFile;

            // Check if the path is a URL
            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                pdfFile = downloadFile(filePath);
            } else {
                pdfFile = new File(filePath);
            }


            //Extract text from PDF document
            String textFromPdf = extractTextFromPdf(pdfFile.getAbsolutePath());
            logger.info("Text from PDF: " + textFromPdf);

            //Extract images and perform OCR to extract text from images
            String ocrText = extractTextFromImages(pdfFile.getAbsolutePath());
            logger.info("OCR Text: " + ocrText);

            // Combine the text from the PDF and OCR text
            String combinedText = textFromPdf + " " + ocrText;
            logger.info("The text extracted from the PDF: " + combinedText);

            runTimeData.setKey(testdata.getValue().toString());
            runTimeData.setValue(combinedText);

            setSuccessMessage("The text was successfully extracted from the PDF and images, and stored in variable: " + testdata.getValue().toString() + ". Value: " + combinedText);
            logger.info("The text was successfully extracted from the PDF and images, and stored in variable: " + testdata.getValue().toString());

            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                pdfFile.delete();
            }

        } catch (Exception e) {
            setErrorMessage("Error during PDF extraction process: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }

    private String extractTextFromPdf(String pdfFilePath) throws IOException {
        // Extract text from PDF
        PDDocument document = Loader.loadPDF(new File(pdfFilePath));
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        logger.info("Text extracted from the PDF: " + text);
        return text;
    }

    private String extractTextFromImages(String pdfFilePath) throws IOException {
        // Extract text from images using OCR
        PDDocument document = Loader.loadPDF(new File(pdfFilePath));
        StringBuilder ocrText = new StringBuilder();

        // Iterate through pages and extract images
        for (PDPage page : document.getPages()) {
            // Get the resources on the page, which include images
            PDResources resources = page.getResources();

            // Iterate through the resources and extract images
            for (COSName name : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(name);

                if (xObject instanceof PDImageXObject) {
                    // Handle images here
                    PDImageXObject imageXObject = (PDImageXObject) xObject;

                    // Convert the PDImageXObject to a BufferedImage
                    BufferedImage bufferedImage = imageXObject.getImage();
                    if(bufferedImage == null) {
                        logger.info("Skipping null image");
                        continue;
                    }

                    // Save the BufferedImage to a temporary file
                    File tempFile = saveBufferedImageToTempFile(bufferedImage);

                    // Pass the temp file to OCR
                    OCRImage imageObj = new OCRImage();
                    imageObj.setOcrImageFile(tempFile); // Set the temporary image file

                    // Perform OCR on the image
                    try {
                        List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
                        if(textPoints != null){
                            for (OCRTextPoint textPoint : textPoints) {
                                ocrText.append(textPoint.getText()).append(" ");
                            }
                        }
                    } catch (Exception ex) {
                        logger.warn("Error during OCR extraction: " + ExceptionUtils.getStackTrace(ex));
                    }

                    // Delete the temporary file after processing
                    tempFile.delete();
                }
            }
        }
        document.close();
        return ocrText.toString();
    }


    private File saveBufferedImageToTempFile(BufferedImage bufferedImage) throws IOException {
        // Create a temporary file
        File tempFile = File.createTempFile("ocr_image", ".png");

        // Write the BufferedImage to the temp file as PNG
        ImageIO.write(bufferedImage, "PNG", tempFile);

        return tempFile;
    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        File tempFile = File.createTempFile("downloaded-", fileName);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}





