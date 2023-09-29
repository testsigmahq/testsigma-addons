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
@Action(actionText = "Select a child/sub table from word document and select or clear a cell."+
        "Document path : Path, Table main heading : main-heading, sub heading : sub-heading, table position under sub heading : Position,"+
        "Perform operation : <operation> on target cell column name : column-name, where column2 name : column2-name value equals column2-value.",
        description = "Selects a child/sub table from word document and select or clear a cell.",
        applicationType = ApplicationType.WEB)
public class CellSelectionAndDeletionV2 extends WebAction {
    @TestData(reference = "Path")
    private com.testsigma.sdk.TestData documentPath;
    @TestData(reference = "main-heading")
    private com.testsigma.sdk.TestData mainHeading;
    @TestData(reference = "sub-heading")
    private com.testsigma.sdk.TestData subHeading;
    @TestData(reference = "Position")
    private com.testsigma.sdk.TestData position;
    @TestData(reference = "<operation>", allowedValues = {"Select", "Clear"})
    private com.testsigma.sdk.TestData operation;
    @TestData(reference = "column-name")
    private com.testsigma.sdk.TestData targetColumn;
    @TestData(reference = "column2-name")
    private com.testsigma.sdk.TestData column;
    @TestData(reference = "column2-value")
    private com.testsigma.sdk.TestData value;

    @Override
    protected Result execute() throws NoSuchElementException {
        CellUtil cellUtil = new CellUtil();
        Result result = Result.FAILED;
        if(cellUtil.CellSelectionAndDeletion(
                documentPath.getValue().toString(),
                mainHeading.getValue().toString(),
                subHeading.getValue().toString(),
                false,
                Integer.parseInt(position.getValue().toString()),
                targetColumn.getValue().toString(),
                column.getValue().toString(),
                value.getValue().toString(),
                operation.getValue().toString()
        )){
            result = Result.SUCCESS;
            setSuccessMessage(cellUtil.getSuccessMessage());
        }else{
            setErrorMessage(cellUtil.getErrorMessage());
        }
        return result;
    }
}

