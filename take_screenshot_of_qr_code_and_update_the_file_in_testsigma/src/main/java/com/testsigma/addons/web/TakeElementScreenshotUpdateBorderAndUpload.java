

package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import okhttp3.*;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;

@Data
@Action(actionText = "Take screenshot of the UI-Component and add border: element, update size and format to jpg and upload it on testsigma uploads with Project_ID: projectid ,Application-ID: applicationid, Upload-Name: uploadname, Upload-EndPoint: url,API-Key: APIKEY, Target-Size: Dimension (width,height) ",
        description = "Take Screenshot of elemet and updates it on testsigma uploads",
        applicationType = ApplicationType.WEB)
public class TakeElementScreenshotUpdateBorderAndUpload extends WebAction {


    @Element(reference = "element")
    private com.testsigma.sdk.Element element;
    @TestData(reference = "projectid")
    private com.testsigma.sdk.TestData projectid;
    @TestData(reference = "applicationid")
    private com.testsigma.sdk.TestData applicationid;
    @TestData(reference = "uploadname")
    private com.testsigma.sdk.TestData uploadname;
    @TestData(reference = "url")
    private com.testsigma.sdk.TestData url;
    @TestData(reference = "APIKEY")
    private com.testsigma.sdk.TestData APIKEY;
    @TestData(reference = "Dimension")
    private com.testsigma.sdk.TestData dimensions;

    @Override
    public com.testsigma.sdk.Result execute() {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {

            File screenshotFile = element.getElement().getScreenshotAs(OutputType.FILE);
            File convertedFile = convertFileToJpg(screenshotFile);
            decorateImage(convertedFile.getAbsolutePath());
            logger.info("Original File path ========" + screenshotFile.getAbsolutePath());


            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            long timeMilli = date.getTime();
            long timeMilli2 = calendar.getTimeInMillis();
            String version = String.valueOf(timeMilli + timeMilli2);

            String bearerKey = APIKEY.getValue().toString().startsWith("Bearer") ? APIKEY.getValue().toString() : "Bearer "+APIKEY.getValue().toString();

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("fileContent", convertedFile.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    convertedFile))
                    .addFormDataPart("projectId", projectid.getValue().toString())
                    .addFormDataPart("name", uploadname.getValue().toString())
                    .addFormDataPart("uploadType", "Attachment")
                    .addFormDataPart("platformType", "TestsigmaLab")
                    .addFormDataPart("isPublic", "true")
                    .addFormDataPart("applicationId", applicationid.getValue().toString())
                    .addFormDataPart("Version", version)
                    .build();
            Request request = new Request.Builder()
                    .url(url.getValue().toString())
                    .method("PUT", body)
                    .addHeader("Authorization", bearerKey)
                    .build();
            Response response = client.newCall(request).execute();
            logger.info("Upload response"+response.toString());
            setSuccessMessage("Successfully uploaded the QR code in Testsigma Upoload" + response.body().string() + " Response code is " + response.code());

        } catch (Exception e) {
            logger.debug(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Operation failed " + e.getMessage());
        }


        return result;
    }

    private File convertFileToJpg(File screenshotFile) throws Exception{
        int targetWidth = Integer.parseInt(dimensions.getValue().toString().split(",")[0].trim());
        int targetHeight = Integer.parseInt(dimensions.getValue().toString().split(",")[1].trim());
        File outputFile = Files.createTempFile("output", ".jpg").toFile();
        try {
            BufferedImage originalImage = Imaging.getBufferedImage(screenshotFile);
            BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

            // Fill the new image with a white background
            for (int y = 0; y < targetHeight; y++) {
                for (int x = 0; x < targetWidth; x++) {
                    newImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }

            // Calculate the position to center the original image
            int x = (targetWidth - originalImage.getWidth()) / 2;
            int y = (targetHeight - originalImage.getHeight()) / 2;

            // Draw the original image onto the new image at the calculated position
            for (int oy = 0; oy < originalImage.getHeight(); oy++) {
                for (int ox = 0; ox < originalImage.getWidth(); ox++) {
                    newImage.setRGB(x + ox, y + oy, originalImage.getRGB(ox, oy));
                }
            }
            logger.info("outputFile = " + outputFile.getAbsolutePath());
            ImageIO.write(newImage, "jpg", outputFile);
            //  Imaging.writeImage(newImage, outputFile, ImageFormats.JPEG);
            //    Imaging.writeImage(newImage, outputFile, null, 1);

            logger.info("resized image saved successfully.");

        }catch (Exception e){
            logger.info("Error in converting file to jpg"+e.getMessage());
            throw e;
        }
        return outputFile;

    }
    private static void decorateImage(String path) {
        try {
            double multiply = 1;
            resize(path, path, multiply);
            addBorder(path, path, 10, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void addBorder(
            String inputImagePath,
            String outputImagePath, int borderLeft, int borderTop) throws IOException {
        File inputFile = new File(inputImagePath);
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedImage source = ImageIO.read(fis);
        int width = source.getWidth();
        int height = source.getHeight();
        int borderedImageWidth = width + (borderLeft * 2);
        int borderedImageHeight = height + (borderTop * 2);
        BufferedImage img = new BufferedImage(borderedImageWidth,
                borderedImageHeight, BufferedImage.TYPE_3BYTE_BGR);
        img.createGraphics();
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, borderedImageWidth, borderedImageHeight);
        g.drawImage(source, borderLeft, borderTop, width + borderLeft,
                height + borderTop, 0, 0, width, height, Color.YELLOW, null);
        ImageIO.write(img, "png", new File(outputImagePath));
    }
    private static void resize(String inputImagePath, String outputImagePath, double percent) throws IOException {
        File inputFile = new File(inputImagePath);
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedImage inputImage = ImageIO.read(fis);
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        resize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
    }
    private static void resize(String inputImagePath, String outputImagePath, int scaledWidth, int scaledHeight) throws IOException {
        // reads input image
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight,    inputImage.getType());
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath.lastIndexOf(".") + 1);
        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
}
