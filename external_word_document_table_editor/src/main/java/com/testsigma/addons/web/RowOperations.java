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
@Action(actionText = "Select a table from word document and edit or verify a row."+
        "Document path : Path, Table heading : Heading,"+
        "Perform operation : <operation> on row with data : row-data where column : column-name value equals column-value.",
        description = "Selects a table from word document and edit or verify a row.",
        applicationType = ApplicationType.WEB)
public class RowOperations extends WebAction {
    @TestData(reference = "Path")
    private com.testsigma.sdk.TestData documentPath;
    @TestData(reference = "Heading")
    private com.testsigma.sdk.TestData heading;
    @TestData(reference = "<operation>", allowedValues = {"Modify","Append","Verify"})
    private com.testsigma.sdk.TestData operation;
    @TestData(reference = "column-name")
    private com.testsigma.sdk.TestData column;
    @TestData(reference = "column-value")
    private com.testsigma.sdk.TestData value;
    @TestData(reference = "row-data")
    private com.testsigma.sdk.TestData data;
    @Override
    protected Result execute() throws NoSuchElementException {
        RowUtil rowUtil = new RowUtil();
        Result result = Result.FAILED;
        if(rowUtil.RowOperations(
                documentPath.getValue().toString(),
                heading.getValue().toString(),
                "",
                true,
                1,
                column.getValue().toString(),
                value.getValue().toString(),
                operation.getValue().toString(),
                data.getValue().toString()
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
