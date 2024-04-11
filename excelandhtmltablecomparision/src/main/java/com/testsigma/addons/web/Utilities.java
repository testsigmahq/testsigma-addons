package com.testsigma.addons.web;

import com.testsigma.sdk.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class Utilities {

    WebDriver driver;
    Logger logger;
    public Utilities(WebDriver driver, Logger logger){
        this.driver = driver;
        this.logger = logger;
    }
    public int getContentHtmlRowCount(List<WebElement> rows){
        int htmlRowCount = rows.size();
        logger.info("Html rows retrieved including empty : "+htmlRowCount);
        if(htmlRowCount <= 6){
            for (WebElement row : rows) {
                String rowText = row.getText().trim();
                if (rowText.matches("&nbsp;|\\s*")) {
                    htmlRowCount--;
                }
            }
            //Ignoring only Add a line row since the footer row was handled
            return --htmlRowCount;
        }
        //Ignoring Add a line row and footer row
        return htmlRowCount-2;
    }

    public WebElement getElementAndHandleStaleException(com.testsigma.sdk.Element element){
        WebElement htmlTable = null;
        int retry = 0;
        while(retry != 3){
            try{
                htmlTable = element.getElement();
                retry = 3;
            }catch(StaleElementReferenceException e){
                retry++;
                logger.debug("Catched StaleElementReferenceException.");
            }
        }
        return htmlTable;
    }

    public File copyFileFromDownloads(String fileFormat) throws Exception{
        String currentWindowHandle = driver.getWindowHandle();

        ((JavascriptExecutor) driver).executeScript("window.open()");
        Set<String> allWindows = driver.getWindowHandles();
        ArrayList<String> tabs = new ArrayList<>(allWindows);
        driver.switchTo().window(tabs.get(tabs.size() - 1));

        driver.navigate().to("chrome://downloads/");
        WebDriverWait ww = new WebDriverWait(driver, Duration.ofSeconds(60));
        ww.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return isFileDownloaded();
            }
        });
        String remoteFilePath = getDownloadedFileLocalPath();
        logger.info("Downloaded file path="+remoteFilePath);
        File downloadedFile = createLocalFileFromDownloadsCopy(remoteFilePath,fileFormat);
        //switch to parent window tab
        driver.switchTo().window(currentWindowHandle);
        return downloadedFile;
    }

    private boolean isFileDownloaded() {

        if (!driver.getCurrentUrl().startsWith("chrome://downloads")) {
            driver.get("chrome://downloads/");
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object obj = js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList')" +
                ".items.filter(e => e.state === 'IN_PROGRESS').map(e => e.filePath || e.file_path || e.fileUrl || e.file_url); ");
        if (obj != null && obj instanceof List && !((List) obj).isEmpty()) {
            return false;
        }
        return true;

    }

    private String getDownloadedFileLocalPath() {


        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object obj = js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items[0].filePath; ");
        return obj.toString();
    }
    private File createLocalFileFromDownloadsCopy(String path, String fileFormat) throws IOException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement elem = (WebElement) js.executeScript("var input = window.document.createElement('INPUT'); " +
                "input.setAttribute('type', 'file'); " +
                "input.hidden = true; " +
                "input.onchange = function (e) { e.stopPropagation() }; " +
                "return window.document.documentElement.appendChild(input); ");

        //elem._execute('sendKeysToElement', {'value': [path ],'text':path})
        elem.sendKeys(path);
        logger.info("File path: "+path);
        long start = System.currentTimeMillis();
        Object result = js.executeAsyncScript("var input = arguments[0], callback = arguments[1]; " +
                        "var reader = new FileReader(); " +
                        "reader.onload = function (ev) { callback(reader.result) }; " +
                        "reader.onerror = function (ex) { callback(ex.message) }; " +
                        "reader.readAsDataURL(input.files[0]); " +
                        "input.remove(); "
                , elem);

        long end = System.currentTimeMillis(); System.out.println("Time taken: "+(end-start));
        if (result == null || !result.toString().startsWith("data:")) {
            throw new RuntimeException("Failed to get file content: " + result);
        }
        String base64String = result.toString().substring(result.toString().indexOf("base64")+7);
        File f = new File(path);
        String fileName = f.getName();
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        File downloadedFile = File.createTempFile(fileName,"."+fileFormat);
        // String data = new String(decodedBytes);
        System.out.println("fileName: "+fileName);
        logger.info("Local path"+downloadedFile.getAbsolutePath());
        Files.write(Paths.get(downloadedFile.getAbsolutePath()), decodedBytes);
        return downloadedFile;
    }

    public File createTempFileFromS3Url(URL preSignedUrl, String extention) throws IOException {
        System.out.println(String.format("Creating File Url : %s with extension : %s ", preSignedUrl, extention));
        logger.info(String.format("Creating File Url : %s with extension : %s ", preSignedUrl, extention));
        File tempFile = File.createTempFile("tempFile", "."+extention);
        try (InputStream in = preSignedUrl.openStream();
             FileOutputStream out = new FileOutputStream(tempFile)) {

            // Copy content from the URL to the temporary file
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("Crated File AT : " + tempFile.getAbsolutePath());
        logger.info("Crated XLSX File : " + tempFile.getAbsolutePath());
        return tempFile;
    }

    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ""; // No extension found
        }
        return fileName.substring(lastDotIndex + 1);
    }

    public int findColumnIndexInExcelSheet(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0);
        int lastCellNum = headerRow.getLastCellNum();
        logger.info("No of excel columns retrieved: "+lastCellNum);
        for (int i = 0; i < lastCellNum; i++) {
            String cellText = headerRow.getCell(i).getStringCellValue();
            logger.info(i+"th column name: "+cellText);
            if (cellText.trim().equals(columnName)) {
                return i;
            }
        }

        return -1; // Column not found
    }



    public String readXLSXCellValue(URL attachmentS3Url, String excelColumnName, int excelRowNumber) throws Exception {

        InputStream inputStream = attachmentS3Url.openStream();
        logger.info("Reading XLSX file from URL :" + attachmentS3Url);
        BufferedInputStream filePase = new BufferedInputStream(inputStream);

        Workbook workbook = new XSSFWorkbook(filePase);

        logger.info("Reading first sheet Excel");
        Sheet sheet = workbook.getSheetAt(0);

        int excelcolumnIndex = findColumnIndexInExcelSheet(sheet, excelColumnName);
        if (excelcolumnIndex == -1) {
            throw new Exception("Column '" + excelColumnName + "' not found in the Sheet.");
        }

        String loggerMessage = String.format("Reading the cell of column index : %d and row number : %d ", excelcolumnIndex, excelRowNumber);
        System.out.println(loggerMessage);
        logger.info(loggerMessage);

        Row excelRow = sheet.getRow(excelRowNumber);
        Cell excelCell = excelRow.getCell(excelcolumnIndex);

        logger.info("Successfully fetched the excel cell");

        // Use DataFormatter to handle different cell types
        DataFormatter formatter = new DataFormatter();
        String excelCellValue = formatter.formatCellValue(excelCell);

        return excelCellValue;
    }

    public String readCSVCellValue(URL attachmentS3Url, String csvColumnName, int csvRowNumber) throws Exception { 
    
        String attachmentExtension = "csv";
        File csvFile = createTempFileFromS3Url(attachmentS3Url, attachmentExtension);
        String delimiter = ","; // CSV delimiter

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Read the headers
            String line = br.readLine();
            String[] headers = line.split(delimiter);

            // Find the index of the column
            int columnIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase(csvColumnName)) {
                    columnIndex = i;
                    break;
                }
            }

            // Check if column exists
            if (columnIndex == -1) {
                throw new Exception("Column '" + csvColumnName + "' not found in the Sheet.");
            }

            String loggerMessage = String.format("Reading the cell of column index : %d and row number : %d ", columnIndex, csvRowNumber);
            System.out.println(loggerMessage);
            logger.info(loggerMessage);

            // Read the specified row and retrieve the value of the specified column
            int currentRow = 1;
            String cellValue = null;
            while ((line = br.readLine()) != null) {
                if (currentRow == csvRowNumber) {
                    String[] rowData = line.split(delimiter);
                    if (columnIndex < rowData.length) {
                        cellValue = rowData[columnIndex];
                        logger.info("Successfully fetched the csv cell value");
                    } else {
                        throw new Exception("Row does not contain enough columns.");
                    }
                    break;
                }
                currentRow++;
            }

            // If specified row number is invalid
            if (currentRow != csvRowNumber) {
                throw new Exception("Row number " + csvRowNumber + " not found.");
            }
            return cellValue;
        } catch (IOException ex) {
            throw ex;
        }
    }
}
