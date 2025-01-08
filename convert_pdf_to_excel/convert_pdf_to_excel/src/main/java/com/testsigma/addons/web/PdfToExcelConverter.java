package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.pdfbox.Loader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import technology.tabula.*;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.List;

@Data
@Action(actionText = "PDF: Convert PDF file at pdf-file-path to Excel and save as output-file-name, storing path in runtime-variable variable-name",
        description = "Extracts content from PDF and converts to Excel, storing path in a runtime variable and it will work in local execution",
        applicationType = ApplicationType.WEB)
public class PdfToExcelConverter extends WebAction {

  @TestData(reference = "pdf-file-path")
  private com.testsigma.sdk.TestData pdfFilePath;

  @TestData(reference = "output-file-name")
  private com.testsigma.sdk.TestData outputFileName;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData runtimeVariable;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  protected Result execute() {
    Result result = Result.SUCCESS;
    try {
      String pdfPath = String.valueOf(pdfFilePath.getValue());
      String fileName = String.valueOf(outputFileName.getValue());

      File pdfFile;
      Path pdfParentPath;

      if (pdfPath.startsWith("http://") || pdfPath.startsWith("https://")) {
        // If the path is a URL, download the PDF and use its filename for output
        pdfFile = downloadPdf(pdfPath);
        pdfParentPath = pdfFile.toPath().getParent(); // Get parent of the downloaded temp file
      } else {
        // Else it's a local file
        pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
          throw new IllegalArgumentException("PDF file does not exist: " + pdfPath);
        }
        pdfParentPath = pdfFile.toPath().getParent();  // Get parent of local file
      }

      // Ensure .xlsx extension
      String outputFileNameWithExtension = fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";

      // Combine parent path with the filename to ensure it's in the same directory
      Path outputPath = pdfParentPath.resolve(outputFileNameWithExtension);
      String absoluteOutputPath = outputPath.toAbsolutePath().toString();

      // Load PDF and extract data
      ObjectExtractor extractor = new ObjectExtractor(Loader.loadPDF(pdfFile));
      SpreadsheetExtractionAlgorithm algo = new SpreadsheetExtractionAlgorithm();

      // Extract tables from the first page
      Page page = extractor.extract(1); // First page
      List<Table> tables = algo.extract(page);

      // Create Excel workbook
      Workbook workbook = new XSSFWorkbook();
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
      }

      // Store output path in runtime variable
      runTimeData.setKey(String.valueOf(runtimeVariable.getValue()));
      runTimeData.setValue(absoluteOutputPath);

      logger.info("PDF converted successfully: " + absoluteOutputPath);
      setSuccessMessage("Successfully converted PDF to Excel at " + absoluteOutputPath);

      // Delete the downloaded file in case of URL
      if (pdfPath.startsWith("http://") || pdfPath.startsWith("https://")) {
        pdfFile.delete();
      }

    } catch (Exception e) {
      logger.warn("PDF conversion failed: " + e);
      setErrorMessage("PDF conversion error: " + e.getMessage());
      result = Result.FAILED;
    }
    return result;
  }

  private File downloadPdf(String pdfUrl) throws IOException {
    // Download PDF file from URL and save locally
    URL url = new URL(pdfUrl);
    String fileName = Paths.get(url.getPath()).getFileName().toString();
    File tempFile = File.createTempFile("downloaded-", fileName);
    try (InputStream in = url.openStream();
         OutputStream out = new FileOutputStream(tempFile)) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
    }
    return tempFile;
  }
}