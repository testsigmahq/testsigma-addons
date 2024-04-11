package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Data
@Action(actionText = "Verify the column column-name data from html table element element-locator <option> the data in column excel-column from latest excel in downloads",
        description = "Comparing table and excel",
        applicationType = ApplicationType.WEB)
public class SpecifiedColumnDataCompareInExcelAndHtmlTable extends WebAction {

    @TestData(reference = "column-name")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "excel-column")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "<option>", allowedValues = {"Equals", "Contains"})
    private com.testsigma.sdk.TestData option;

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
        String columnName = testData.getValue().toString();
        String excelColumnName = testData2.getValue().toString();
        String selectedOption = option.getValue().toString();
        boolean comparisonResult = true;
        try {
            logger.info("Initiated execution");
            File downloadedExcelFile = utilities.copyFileFromDownloads("xlsx");
            logger.info("Local path"+downloadedExcelFile.getAbsolutePath());

            List<WebElement> htmlRows = htmlTable.findElements(By.tagName("tr"));
            int htmlRowCount = utilities.getContentHtmlRowCount(htmlRows);

            FileInputStream excelFile = new FileInputStream(downloadedExcelFile);
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            int excelRowCount = sheet.getPhysicalNumberOfRows();
            int columnIndexInHtml = findColumnIndexInHtmlTable(htmlTable, columnName);
            int columnIndexInExcel = utilities.findColumnIndexInExcelSheet(sheet, excelColumnName);
            if(excelRowCount == htmlRowCount){
                if (columnIndexInHtml == -1) {
                    result = Result.FAILED;
                    setErrorMessage(("Column '" + columnName + "' not found in the HTML table."));
                } else if (columnIndexInExcel == -1) {
                    result = Result.FAILED;
                    setErrorMessage(("Column '" + excelColumnName + "' not found in the Excel file."));
                } else{
                    List<String> defaults = List.of("Weight", "Volume", "Volumetric Weight", "Min Temperature", "Max Temperature");
                    List<WebElement> rows = htmlTable.findElements(By.tagName("tbody")).get(0).findElements(By.tagName("tr"));
                    for (int i = 0; i < htmlRowCount-1; i++) {
                        WebElement htmlRow = rows.get(i);
                        List<WebElement> cells = htmlRow.findElements(By.tagName("td"));
                        String htmlColumnValue = cells.get(columnIndexInHtml).getText();

                        //handling empty rows from excel and zero values from html
                        if(defaults.contains(columnName) && htmlColumnValue.equals("0.00"))
                            htmlColumnValue = "";

                        Row excelRow = sheet.getRow(i+1);
                        Cell excelCell = excelRow.getCell(columnIndexInExcel);

                        // Use DataFormatter to handle different cell types
                        DataFormatter formatter = new DataFormatter();
                        String excelColumnValue = formatter.formatCellValue(excelCell);

                        //logger.info("Excel column value:"+excelColumnValue+",Html column value:"+htmlColumnValue+" at"+(i+1)+"th row");

                        if(selectedOption.equals("Equals")){
                            if (!htmlColumnValue.trim().equals(excelColumnValue.trim())) {
                                result = Result.FAILED;
                                comparisonResult = false;
                                setErrorMessage("Both column values are not same at row "+(i+1)+", Excel column value:"+excelColumnValue+", Html column value:"+htmlColumnValue);
                                break;
                            }
                        } else {
                            if (!htmlColumnValue.trim().contains(excelColumnValue.trim())) {
                                result = Result.FAILED;
                                comparisonResult = false;
                                setErrorMessage("Contains failed at row "+(i+1)+", Excel column value:"+excelColumnValue+", Html column value:"+htmlColumnValue);
                                break;
                            }
                        }

                    }
                    if(comparisonResult){
                        setSuccessMessage("Comparison successful, the given column data is same in excel and html table");
                    }
                }
            } else {
                setErrorMessage("No of rows in excel and table are not equal, Row count of html table:"+htmlRowCount+" and excel:"+excelRowCount);
                result = Result.FAILED;
            }

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("Unable to access the excel or html table. Exception : "+e.getMessage());

        }
        return result;
    }
    private int findColumnIndexInHtmlTable(WebElement table, String columnName) {
        // Assuming the first row of the table contains the column names
        WebElement headerRow = table.findElements(By.tagName("thead")).get(0).findElements(By.tagName("tr")).get(0);
        int lastCellNum = headerRow.findElements(By.tagName("th")).size();
        logger.info("No of html columns retrieved: "+lastCellNum);
        for (int i = 0; i < lastCellNum; i++) {
            String cellText = headerRow.findElements(By.tagName("th")).get(i).getText();
            logger.info(i+"th column name: "+cellText);
            if (cellText.trim().equals(columnName)) {
                return i;
            }
        }

        return -1; // Column not found
    }
}