package com.testsigma.addons.web;


import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import utility.RowUtil;


@Data
@Action(actionText = "Select a child/sub table from word document and add a row in specific position."+
        "Document path : Path , Table main heading : main-heading, sub heading : sub-heading, table position under sub heading : Position,"+
        "Add row with data : row-data at position row-position.",
        description = "Selects a child/sub table from word document and add a row in specific position.",
        applicationType = ApplicationType.WEB)
public class RowAdditionV2 extends WebAction {
    @TestData(reference = "Path")
    private com.testsigma.sdk.TestData documentPath;
    @TestData(reference = "main-heading")
    private com.testsigma.sdk.TestData mainHeading;
    @TestData(reference = "sub-heading")
    private com.testsigma.sdk.TestData subHeading;
    @TestData(reference = "Position")
    private com.testsigma.sdk.TestData tablePosition;
    @TestData(reference = "row-data")
    private com.testsigma.sdk.TestData data;
    @TestData(reference = "row-position")
    private com.testsigma.sdk.TestData rowPosition;
    @Override
    protected Result execute() throws NoSuchElementException {
        RowUtil rowUtil = new RowUtil();
        Result result = Result.FAILED;
        if(rowUtil.RowAddition(
                documentPath.getValue().toString(),
                mainHeading.getValue().toString(),
                subHeading.getValue().toString(),
                false,
                Integer.parseInt(tablePosition.getValue().toString()),
                data.getValue().toString(),
                Integer.parseInt(rowPosition.getValue().toString())
        )){
            result = Result.SUCCESS;
            setSuccessMessage(rowUtil.getSuccessMessage());
            logger.info(rowUtil.getSuccessMessage());
        }else{
            setErrorMessage(rowUtil.getErrorMessage());
            logger.info(rowUtil.getErrorMessage());
        }
        return result;
    }
}

