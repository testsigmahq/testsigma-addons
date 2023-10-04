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
@Action(actionText = "Select a table from word document and select or delete a row."+
        "Document path : Path, Table heading : Heading,"+
        "Perform operation : <operation> on row, where column : column-name value equals column-value.",
        description = "Selects a table from word document and select or delete a row.",
        applicationType = ApplicationType.WEB)
public class RowSelectionAndDeletion extends WebAction {
    @TestData(reference = "Path")
    private com.testsigma.sdk.TestData documentPath;
    @TestData(reference = "Heading")
    private com.testsigma.sdk.TestData heading;
    @TestData(reference = "<operation>", allowedValues = {"Select","Delete"})
    private com.testsigma.sdk.TestData operation;
    @TestData(reference = "column-name")
    private com.testsigma.sdk.TestData column;
    @TestData(reference = "column-value")
    private com.testsigma.sdk.TestData value;
    @Override
    protected Result execute() throws NoSuchElementException {
        RowUtil rowUtil = new RowUtil();
        Result result = Result.FAILED;
        if(rowUtil.RowSelectionAndDeletion(
                documentPath.getValue().toString(),
                heading.getValue().toString(),
                "",
                true,
                1,
                operation.getValue().toString(),
                column.getValue().toString(),
                value.getValue().toString()
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
