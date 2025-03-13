package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Action(actionText = "Convert base64 code base64-data into file with name (Ex:output.pdf) file-name and store " +
        "filepath in runtime variable variable-name",
        description = "Converts any type of file's base64 code into that type of file with given name and extension" +
                " and stores the file path in run time variable",
        applicationType = ApplicationType.WEB)
public class DecodeBase64 extends WebAction {

    @TestData(reference = "base64-data")
    private com.testsigma.sdk.TestData base64Data_;

    @TestData(reference = "file-name")
    private com.testsigma.sdk.TestData fileName_;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variable_;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        String base64Data = base64Data_.getValue().toString();
        String fileName = fileName_.getValue().toString();
        String variable = variable_.getValue().toString();

        try {
            String[] list = fileName.split("\\.");
            if (list.length == 2) {
                File tempFile = File.createTempFile(
                                    list[0], "." + list[1]
                                );
                String filePath = tempFile.getAbsolutePath();
                decodeBase64ToPDF(base64Data, filePath);
                runTimeData.setKey(variable);
                runTimeData.setValue(filePath);
                setSuccessMessage(String.format("Successfully converted the base64 data to the file with" +
                        " name %s and stored the file path <b>%s</b> in run time variable <b>%s</b>", fileName,
                        filePath, variable));
            } else {
                result = Result.FAILED;
                setErrorMessage("Invalid file name input, file name format should be in filname.extension format (Ex: output.pdf)");
            }
        } catch (RuntimeException e) {
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception occurred: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Unable to perform the operation");
        }
        return result;
    }

    public void decodeBase64ToPDF(String base64Str, String outputFilePath) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Str);
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(decodedBytes);
        } catch (IOException e) {
            logger.info("Exception occurred while decoding : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to decode the base64 string to given file");
            throw new RuntimeException("Decoding error");
        }
    }
}
