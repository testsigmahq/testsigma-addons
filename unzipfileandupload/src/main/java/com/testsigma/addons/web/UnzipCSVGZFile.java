package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;

import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Data
@Action(actionText = "Unzip the .CSV.GZ file absolutefilepath to destination destfilepath and store the filepath into a variable testdata",
        description = "Unzip the .CSV.GZ file and store the extracted file path",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class UnzipCSVGZFile extends WebAction {

    @TestData(reference = "absolutefilepath")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "destfilepath")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "testdata" , isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            String zipFilePath = testData1.getValue().toString();
            String destDir = testData2.getValue().toString();
            String fileNameUpload = "output" + System.currentTimeMillis() + ".csv";
            String filePath = destDir + File.separator + fileNameUpload;
            unzip(zipFilePath, filePath);
            logger.info("Storing the filepath in runtime variable");
            runTimeData.setValue(filePath);
            runTimeData.setKey(testData3.getValue().toString());
            logger.info("Stored successfully");
            setSuccessMessage("File was unzipped and stored successfully in : " + testData3.getValue().toString() + " and the filepath is :" + filePath);
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
        }
        return result;
    }
    private void unzip(String zipFilePath, String destFilepath) throws IOException {
        logger.info("Extracting the file");
        FileInputStream fis = new FileInputStream(zipFilePath);
        GZIPInputStream gis = new GZIPInputStream(fis);
        FileOutputStream fos = new FileOutputStream(destFilepath);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        gis.close();
        fis.close();
        logger.info("Successfully extracted to location: " + destFilepath);
    }
}