

package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;

@Data
@Action(actionText = "Take screenshot of the UI-Component: element, update size and format to jpg and upload it on testsigma uploads with Project_ID: projectid ,Application-ID: applicationid, Upload-Name: uploadname, Upload-EndPoint: url,API-Key: APIKEY, Target-Size: Dimension (width,height) ",
        description = "Take Screenshot of elemet and updates it on testsigma uploads",
        applicationType = ApplicationType.WEB)
public class TakeElementScreenshotUpdateAndUpload extends WebAction {


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
            logger.info("Original File path ========" + screenshotFile.getAbsolutePath());
            File convertedFile = convertFileToJpg(screenshotFile);

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
}
