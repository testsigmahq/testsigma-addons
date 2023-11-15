package com.testsigma.addons.android;


import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.NoSuchElementException;



@Action(
        actionText = "Upload file file-path to device path device-path",
        description = "Uploading a file to an android device in cloud with specifying remote-path",
        applicationType = ApplicationType.ANDROID
)
public class UploadFileToAndroidWithRemotePath extends AndroidAction {
    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData file;

    @TestData(reference = "device-path")
    private com.testsigma.sdk.TestData remotePath;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result;

        AndroidDriver androidDriver = (AndroidDriver)this.driver;
        UploadFileToAndroidUtility utility = new UploadFileToAndroidUtility();

        boolean uploadResult = utility.performUpload(file.getValue().toString(),remotePath.getValue().toString(),androidDriver);
        logger.info("Temporary file path on the local device: "+utility.getTempFilePath());

        if(uploadResult){
            result = Result.SUCCESS;
            logger.info("File uploaded to the android device in the cloud");
            setSuccessMessage(utility.getSuccessMessage());
        } else {
            result = Result.FAILED;
            logger.info(utility.getErrorMessage());
            setErrorMessage(utility.getErrorMessage());
        }

        return result;
    }

}