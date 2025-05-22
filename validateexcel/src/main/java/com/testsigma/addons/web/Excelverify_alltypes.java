package com.testsigma.addons.web;


import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Excelverify_alltypes {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		String excelFilePath = "C:\\Users\\nagabhushanam.NAGABHUSHANAM-P\\Downloads\\testexcel.xlsx";
		
		try {
            // Load the Excel file
            FileInputStream excelFile = new FileInputStream(new File(excelFilePath));
            XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
            XSSFSheet sheet = workbook.getSheetAt(0); // Get the first sheet

            // Specify the row and column index of the desired cell
            int targetRow = 1;  // Example row index (0-based)
            int targetColumn = 1;  // Example column index (0-based)

            // Get the desired cell
            XSSFRow row = sheet.getRow(targetRow);
            XSSFCell cell = row.getCell(targetColumn);

            if (cell != null) {
                String cellValue;

                switch (cell.getCellType()) {
                    case STRING:
                        cellValue = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            cellValue = cell.getDateCellValue().toString();
                        } else {
                            cellValue = Double.toString(cell.getNumericCellValue());
                        }
                        break;
                    case BOOLEAN:
                        cellValue = Boolean.toString(cell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        cellValue = cell.getCellFormula();
                        break;
                    default:
                        cellValue = "N/A";
                }
                // Print the cell value with appropriate data type
                System.out.println("Cell Value: " + cellValue);
            } else {
                System.out.println("Cell not found");
            }

            // Close the workbook
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}

