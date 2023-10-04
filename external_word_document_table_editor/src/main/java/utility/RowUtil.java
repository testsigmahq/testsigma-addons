package utility;

import lombok.Data;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openqa.selenium.NoSuchElementException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.InputMismatchException;
import java.util.List;

@Data
public class RowUtil {
    private String errorMessage;
    private String successMessage;
    WordTableEditorUtil wordTableEditorUtil = new WordTableEditorUtil();
    public boolean RowAddition(
            String documentPath,
            String mainHeading,
            String subHeading,
            boolean onlyMainHeading,
            int tablePosition,
            String data,
            int position
    ){
        boolean result = false;
        List<String> dataList = List.of(data.split("--"));
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
            if(position < 0 || position > table.getNumberOfRows()){
                throw new InvalidParameterException("Invalid position");
            }
            XWPFTableRow oldRow = table.getRow(1);
            CTRow ctrow = CTRow.Factory.parse(oldRow.getCtRow().newInputStream());
            XWPFTableRow newRow = new XWPFTableRow(ctrow, table);
            int i = 0;
            if(dataList.size() != oldRow.getTableCells().size()){
                throw new InputMismatchException("Given data items size is incompatible with specified table row");
            }
            for (XWPFTableCell cell : newRow.getTableCells()) {
                    cell.removeParagraph(0);
                    cell.setText("");
            }

            for (int index = 0; index < newRow.getTableCells().size(); index++) {
                XWPFParagraph paragraph = newRow.getCell(index).addParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(dataList.get(index));
            }
            table.addRow(newRow, position);
            FileOutputStream fos = new FileOutputStream(documentPath);
            document.write(fos);
            result = true;
            this.successMessage = "Row added successfully";
            fos.close();
            document.close();
        }catch(NoSuchElementException | InputMismatchException e){
            this.errorMessage = e.getMessage();
        }
        catch(IOException | RuntimeException | XmlException e){
            this.errorMessage = e.getMessage();
        }
        return result;
    }

    public boolean RowOperations(
            String documentPath,
            String mainHeading,
            String subHeading,
            boolean onlyMainHeading,
            int tablePosition,
            String column,
            String value,
            String operation,
            String data
    ){
        boolean result = false;
        List<String> dataList = List.of(data.split("--"));
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
            int rowIndex = wordTableEditorUtil.getRowIndex(table,column,value);
            if(rowIndex == -1){
                throw new NoSuchElementException("Specified row not found");
            }
            XWPFTableRow row = table.getRow(rowIndex);
            if(dataList.size() != row.getTableCells().size()){
                throw new InputMismatchException("Given data items size is incompatible with specified row");
            }
            int index = 0;
            for(XWPFTableCell cell : row.getTableCells()){
                if(!wordTableEditorUtil.tableCellEditor(cell,operation,dataList.get(index++))){
                    throw new RuntimeException(operation+" on specified row failed");
                }
            }
            FileOutputStream fos = new FileOutputStream(documentPath);
            document.write(fos);
            result = true;
            this.successMessage = operation+" operation on row is successful";
            fos.close();
            document.close();

        }catch( NoSuchElementException | InputMismatchException e){
            this.errorMessage = e.getMessage();
        }
        catch(IOException | RuntimeException e){
            this.errorMessage = e.getMessage();
        }
        return result;
    }
    public boolean RowSelectionAndDeletion(
            String documentPath,
            String mainHeading,
            String subHeading,
            boolean onlyMainHeading,
            int tablePosition,
            String operation,
            String column,
            String value
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
            int rowIndex = wordTableEditorUtil.getRowIndex(table,column,value);
            if(rowIndex == -1){
                throw new NoSuchElementException("Specified row not found");
            }
            if(operation.equals("Delete")){
                table.removeRow(rowIndex);
                FileOutputStream fos = new FileOutputStream(documentPath);
                document.write(fos);
                fos.close();
                document.close();
                result = true;
                this.successMessage = "Row deletion successful";
            }else{
                String rowData = "Selected row data : ";
                for(XWPFTableCell cell : table.getRow(rowIndex).getTableCells()){
                    rowData = rowData.concat(cell.getText()+", ");
                }
                result = true;
                this.successMessage = rowData;
            }

        }catch( NoSuchElementException | InputMismatchException e){
            this.errorMessage = e.getMessage();
        }
        catch(IOException | RuntimeException e){
            this.errorMessage = e.getMessage();
        }
        return result;
    }
}
