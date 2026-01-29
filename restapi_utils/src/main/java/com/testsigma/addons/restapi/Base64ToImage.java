package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.UUID;

@Data
@Action(
        actionText = "Convert Base64 string base64-string to image file and store path in runtime variable variable-name",
        description = "Converts a Base64 string into an image file with a unique name, and stores the file path in a runtime variable.",
        applicationType = ApplicationType.REST_API
)
public class Base64ToImage extends RestApiAction {

    @TestData(reference = "base64-string")
    private com.testsigma.sdk.TestData base64String;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData targetVariableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Starting Base64 to Unique File Conversion");
        Result result = Result.SUCCESS;

        try {
            // Retrieve inputs
            String b64Data = base64String.getValue().toString();
            String varName = targetVariableName.getValue().toString();

            logger.info("Received Base64 string length: " + b64Data.length());
            logger.info("Runtime variable name: " + varName);

            // Determine Extension and sanitize data
            String extension = ".png";

            logger.info("Checking Base64 header for file extension...");

            if (b64Data.contains("data:image/") && b64Data.contains(";base64,")) {
                try {
                    String header = b64Data.split(";base64,")[0];
                    String type = header.split("/")[1]; // "jpeg", "png", "gif", etc.
                    extension = "." + type;
                    if (extension.equalsIgnoreCase(".jpeg"))
                        extension = ".jpg";

                    logger.info("Extracted extension from header: " + extension);
                } catch (Exception ex) {
                    logger.warn("Failed to extract extension from header, defaulting to .png");
                    logger.warn(ExceptionUtils.getStackTrace(ex));
                }
            }

            // Remove header
            if (b64Data.contains(",")) {
                b64Data = b64Data.split(",")[1];
            }

            // Remove whitespace
            b64Data = b64Data.replaceAll("\\s", "");

            logger.info("Sanitized Base64 string length: " + b64Data.length());

            // Generate Unique Filename
            String uniqueFileName = "ts_img_" + System.currentTimeMillis() + "_"
                    + UUID.randomUUID().toString().substring(0, 8) + extension;

            logger.info("Generated unique filename: " + uniqueFileName);

            // Determine Path
            String currentDir = System.getProperty("user.dir");
            File destinationFile = new File(currentDir, uniqueFileName);
            String absolutePath = destinationFile.getAbsolutePath();

            logger.info("Saving file to: " + absolutePath);

            // Decode and Write
            byte[] imageBytes = Base64.getDecoder().decode(b64Data);

            try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                fos.write(imageBytes);
                logger.info("Image file successfully written.");
            }

            runTimeData.setKey(varName);
            runTimeData.setValue(absolutePath);
            logger.info("Stored image file path in runtime variable: " + varName);

            setSuccessMessage("Saved image as " + uniqueFileName + " and stored path in variable: " + varName + ". " + varName + " = " + absolutePath);
            logger.info(" Base64 Conversion Completed Successfully");

        } catch (Exception e) {
            logger.warn("Unexpected error while saving Base64 image. Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to save file: " + ExceptionUtils.getMessage(e));
            result =  Result.FAILED;
        }

        return result;
    }
}
