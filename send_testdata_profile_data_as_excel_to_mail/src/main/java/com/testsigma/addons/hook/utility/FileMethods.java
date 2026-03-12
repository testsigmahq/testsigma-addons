package com.testsigma.addons.hook.utility;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileMethods {

    public void writeToExcelFile(File file, String response, XSSFWorkbook workbook) throws IOException {

        // Getting test data profile name
        JSONObject obj = new JSONObject(response);
        String testDataName = obj.get("testDataName").toString();

        try {
            XSSFSheet sheet = workbook.createSheet(testDataName);
            JSONArray arr = obj.getJSONArray("data");
            JSONObject columnNames = (JSONObject) arr.getJSONObject(0).get("data");
            // Adding all the keys as column
            int setNameRowId = 0;
            int SetValueCellId = 0;
            XSSFRow row = sheet.createRow(setNameRowId++);
            Cell cell = row.createCell(SetValueCellId++);
            cell.setCellValue("Set Name");
            for (String key : columnNames.keySet()) {
                Cell headerCell = row.createCell(SetValueCellId++);
                headerCell.setCellValue(key);
            }

            // Adding the data set name and its corresponding values
            for (int i=0; i <arr.length(); i++) {
                int dataSetRow = setNameRowId + i;
                int dataSetCell = 0;
                XSSFRow setRow = sheet.createRow(dataSetRow++);
                String setValues = arr.getJSONObject(i).get("name").toString();
                Cell setValueCell = setRow.createCell(dataSetCell++);
                setValueCell.setCellValue(setValues);
                JSONObject rowValues = (JSONObject) arr.getJSONObject(i).get("data");
                for (String key : rowValues.keySet()) {
                    Cell newCell = setRow.createCell(dataSetCell++);
                    newCell.setCellValue(rowValues.get(key).toString());
                }
            }
            try (FileOutputStream out = new FileOutputStream(new File(String.valueOf(file)))) {
                workbook.write(out);
                out.close();
            } catch (Exception error) {
                error.printStackTrace();
            }
        } catch (Exception error){
            error.printStackTrace();
        }

    }
}
