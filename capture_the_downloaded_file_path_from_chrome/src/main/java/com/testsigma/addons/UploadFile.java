package com.testsigma.addons;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "File Upload: Upload file File-Path to the element element-name",
        description = "This addon will upload the file to the file element",
        applicationType = ApplicationType.WEB)
public class UploadFile extends WebAction {
    @TestData(reference = "File-Path",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData filePath;

    @Element(reference = "element-name")
   private com.testsigma.sdk.Element element;


    @Override
    protected Result execute() throws NoSuchElementException {

        Result result = Result.SUCCESS;
        try{
            getElement(element).sendKeys(filePath.getValue().toString());
            setSuccessMessage("Successfully uploaded the file to the element:");
        }catch (Exception e){
            logger.info("Unable to upload the file"+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to upload the file::"+e.getMessage());
            result = Result.FAILED;
        }


        return result;
    }
}
