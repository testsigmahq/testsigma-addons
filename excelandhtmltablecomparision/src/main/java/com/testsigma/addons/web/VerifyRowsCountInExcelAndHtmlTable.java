package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import lombok.Data;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;


@Data
@Action(actionText = "Verify the rows are equal in downloaded excel file and html table element element-locator",
        description = "Comparing table and excel",
        applicationType = ApplicationType.WEB)
public class VerifyRowsCountInExcelAndHtmlTable extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        Utilities utilities = new Utilities(driver,logger);
        WebElement htmlTable = utilities.getElementAndHandleStaleException(element);
        if(htmlTable == null){
            throw new NoSuchElementException("HTML table not found");
        }
        try {
            logger.info("Initiated execution");
            File downloadedExcelFile = utilities.copyFileFromDownloads("xlsx");
            logger.info("Local path"+downloadedExcelFile.getAbsolutePath());
            List<WebElement> rows = htmlTable.findElements(By.tagName("tr"));
            int htmlRowCount = utilities.getContentHtmlRowCount(rows);

            // Load the Excel file
            FileInputStream excelFile = new FileInputStream(downloadedExcelFile);
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            int excelRowCount = sheet.getPhysicalNumberOfRows();
            if (excelRowCount == htmlRowCount){
                setSuccessMessage("The rows in the excel and the given table are equal");
            } else{
                setErrorMessage("No of rows in excel and table are not equal, Row count of html table:"+htmlRowCount+" and excel:"+excelRowCount);
                result = Result.FAILED;
            }
        } catch (Exception e) {
            setErrorMessage("Unable to access the excel or html table. Exception : "+e.getMessage());
            return Result.FAILED;
        }
        return result;
    }
}