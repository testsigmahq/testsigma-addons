package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import utility.CellUtil;


@Data
@Action(actionText = "Select a table from word document and edit or verify a cell."+
        "Document path : Path, Table heading : Heading,"+
        "Perform operation : <operation> on target cell column : column-name with value : Value,"+
        "where column2-name value equals column2-value.",
        description = "Selects a table from word document and edit or verify a cell.",
        applicationType = ApplicationType.WEB)
public class CellOperations extends WebAction {
    @TestData(reference = "Path")
    private com.testsigma.sdk.TestData documentPath;
    @TestData(reference = "Heading")
    private com.testsigma.sdk.TestData heading;
    @TestData(reference = "<operation>", allowedValues = {"Modify","Append","Verify"})
    private com.testsigma.sdk.TestData operation;
    @TestData(reference = "column-name")
    private com.testsigma.sdk.TestData targetColumn;
    @TestData(reference = "column2-name")
    private com.testsigma.sdk.TestData column;
    @TestData(reference = "column2-value")
    private com.testsigma.sdk.TestData value;
    @TestData(reference = "Value")
    private com.testsigma.sdk.TestData data;
    @Override
    protected Result execute() throws NoSuchElementException {
        CellUtil cellUtil = new CellUtil();
        Result result = Result.FAILED;
        if(cellUtil.CellOperations(
              documentPath.getValue().toString().trim(),
              heading.getValue().toString(),
                "",
                true,
                1,
                targetColumn.getValue().toString(),
                column.getValue().toString(),
                value.getValue().toString(),
                operation.getValue().toString(),
                data.getValue().toString()
        )){
            result = Result.SUCCESS;
            setSuccessMessage(cellUtil.getSuccessMessage());
            logger.info(cellUtil.getSuccessMessage());
        }else{
            setErrorMessage(cellUtil.getErrorMessage());
            logger.info(cellUtil.getErrorMessage());
        }
        return result;
    }
}
