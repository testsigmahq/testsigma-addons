package utility;


import lombok.Data;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;

import com.testsigma.sdk.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Data
public class CellUtil {
    private String errorMessage;
    private String successMessage;
    WordTableEditorUtil wordTableEditorUtil = new WordTableEditorUtil();
    public boolean CellOperations(
            String documentPath,
            String mainHeading,
            String subHeading,
            boolean onlyMainHeading,
            int tablePosition,
            String targetColumn,
            String column,
            String value,
            String operation,
            String data
    ){
        boolean result = false;
        try{
            FileInputStream fis = new FileInputStream(documentPath);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();
            XWPFTable table = wordTableEditorUtil.getTable(
                    document,
                    mainHeading,
                    subHeading,
                    onlyMainHeading,
                    tablePosition
            );
            if(table == null){
                throw new NoSuchElementException("Table not found");
            }
            XWPFTableCell cell = wordTableEditorUtil.getTableCell(
                    table,
                    targetColumn,
                    column,
                    value
            );
            if(cell == null){
                throw new NoSuchElementException("Specified cell not found");
            }
            if(wordTableEditorUtil.tableCellEditor(cell,operation,data)){
                FileOutputStream fos = new FileOutputStream(documentPath);
                document.write(fos);
                result = true;
                this.successMessage = operation+" operation on cell is successful";
                fos.close();
                document.close();
            }

        }catch(IOException | NoSuchElementException e){
            this.errorMessage = e.getMessage();
        }
        return result;
    }
    public boolean CellSelectionAndDeletion(
            String documentPath,
            String mainHeading,
            String subHeading,
            boolean onlyMainHeading,
            int tablePosition,
            String targetColumn,
            String column,
            String value,
            String operation
    ){
        boolean result = false;
        try{
            FileInputStream fis = new FileInputStream(documentPath);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();
            XWPFTable table = wordTableEditorUtil.getTable(
                    document,
                    mainHeading,
                    subHeading,
                    onlyMainHeading,
                    tablePosition
            );
            if(table == null){
                throw new NoSuchElementException("Table not found");
            }
            XWPFTableCell cell = wordTableEditorUtil.getTableCell(
                    table,
                    targetColumn,
                    column,
                    value
            );
            if(cell == null){
                throw new NoSuchElementException("Specified cell not found");
            }
            if(operation.equals("Select")){
                result = true;
                this.successMessage = "value found: "+cell.getText();
            }else{
                if(wordTableEditorUtil.tableCellEditor(cell,"Clear","")){
                    FileOutputStream fos = new FileOutputStream(documentPath);
                    document.write(fos);
                    result = true;
                    this.successMessage = "Target cell cleared successfully";
                    fos.close();
                    document.close();
                }
            }

        }catch(IOException | NoSuchElementException e){
            this.errorMessage = e.getMessage();
        }

        return result;
    }
}
