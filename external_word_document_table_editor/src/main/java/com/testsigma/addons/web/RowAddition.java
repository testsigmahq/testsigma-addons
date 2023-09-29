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
@Action(actionText = "Select a table from word document and add a row in specific position."+
        "Document path : Path , Table heading : Heading,"+
        "Add row with data : row-data at position row-position.",
        description = "Selects a table from word document and add a row in specific position.",
        applicationType = ApplicationType.WEB)
public class RowAddition extends WebAction {
    @TestData(reference = "Path")
    private com.testsigma.sdk.TestData documentPath;
    @TestData(reference = "Heading")
    private com.testsigma.sdk.TestData heading;
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
                heading.getValue().toString(),
                "",
                true,
                1,
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
