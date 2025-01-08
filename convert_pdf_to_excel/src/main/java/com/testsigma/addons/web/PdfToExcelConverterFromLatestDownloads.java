package com.testsigma.addons.web;


import com.testsigma.addons.web.utils.PdfAndDocUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.Loader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Data
@Action(actionText = "PDF: Convert latest downloaded PDF to Excel and save as output-file-name, storing path in runtime-variable variable-name",
        description = "Converts the latest downloaded PDF to Excel, storing the path in a runtime variable",
        applicationType = ApplicationType.WEB)
public class PdfToExcelConverterFromLatestDownloads extends WebAction {

    @TestData(reference = "output-file-name")
    private com.testsigma.sdk.TestData outputFileName;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        PdfAndDocUtilities pdfAndDocUtilities = new PdfAndDocUtilities(driver,logger);
        File downloadedPdfFile = null;
        Workbook workbook = null;
        try {
            String fileName = String.valueOf(outputFileName.getValue());
            logger.info("Starting PDF to Excel conversion for latest downloaded PDF. Output Filename: " + fileName);
            downloadedPdfFile = pdfAndDocUtilities.copyFileFromDownloads("pdf",null);
            if(downloadedPdfFile==null){
                setErrorMessage("Could not find any latest pdf file in the downloads folder");
                return Result.FAILED;
            }
            logger.info("Downloaded PDF file found at: " + downloadedPdfFile.getAbsolutePath());
            Path pdfParentPath = downloadedPdfFile.toPath().getParent();

            // Ensure .xlsx extension
            String outputFileNameWithExtension = fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";
            // Combine parent path with the filename to ensure it's in the same directory
            Path outputPath = pdfParentPath.resolve(outputFileNameWithExtension);
            String absoluteOutputPath = outputPath.toAbsolutePath().toString();

            // Load PDF and extract data
            ObjectExtractor extractor = new ObjectExtractor(Loader.loadPDF(downloadedPdfFile));
            SpreadsheetExtractionAlgorithm algo = new SpreadsheetExtractionAlgorithm();

            // Extract tables from the first page
            Page page = extractor.extract(1); // First page
            List<Table> tables = algo.extract(page);

            // Create Excel workbook
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("PDF Data");

            // Fill the sheet with table data
            int rowIndex = 0;
            for (Table table : tables) {
                for (List<RectangularTextContainer> row : table.getRows()) {
                    Row excelRow = sheet.createRow(rowIndex++);
                    int colIndex = 0;
                    for (RectangularTextContainer cell : row) {
                        Cell excelCell = excelRow.createCell(colIndex++);
                        excelCell.setCellValue(cell.getText());
                    }
                }
            }

            // Write to Excel file
            try (FileOutputStream fileOut = new FileOutputStream(absoluteOutputPath)) {
                workbook.write(fileOut);
                logger.info("PDF converted successfully to: " + absoluteOutputPath);

            }


            // Store output path in runtime variable
            runTimeData.setKey(String.valueOf(runtimeVariable.getValue()));
            runTimeData.setValue(absoluteOutputPath);

            setSuccessMessage("Successfully converted PDF to Excel at " + absoluteOutputPath);


        }
        catch (IllegalArgumentException e) {
            logger.warn("Invalid PDF file : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Invalid PDF file: " + e.getMessage());
            result = Result.FAILED;

        }catch (IOException e) {
            logger.warn("IO Exception occurred during PDF to excel conversion: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("An IO error occurred during PDF to Excel conversion: " + e.getMessage());
            result = Result.FAILED;

        }
        catch (Exception e) {
            logger.warn("An unexpected error occurred during PDF to Excel conversion: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("An unexpected error occurred during PDF to Excel conversion: " + e.getMessage());
            result = Result.FAILED;
        }
        finally {
            if(workbook!=null){
                try{
                    workbook.close();
                } catch (IOException e) {
                    logger.warn("Error while closing the workbook resource :"+ExceptionUtils.getStackTrace(e));
                }
            }
        }
        return result;
    }
}