package utility;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
public class WordTableEditorUtil {

    public XWPFTable getTable(
            XWPFDocument document,
            String targetMainHeading,
            String targetHeading,
            boolean onlyMainHeading,
            int tablePosition
    ){
        boolean mainHeadingFound = false;
        boolean headingFound = false;
        int tableCount = 0;
        XWPFTable table = null;
        if(tablePosition < 1){
            throw new InvalidParameterException("Invalid table position. Must be greater than 0");
        }
        Iterator<IBodyElement> elements = document.getBodyElementsIterator();
        while (elements.hasNext()) {
            IBodyElement element = elements.next();
            if (element instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) element;
                String heading = paragraph.getText().trim();
                if(mainHeadingFound && heading.equals(targetHeading)){
                    headingFound = true;
                }else if (heading.equals(targetMainHeading)) {
                    mainHeadingFound = true;
                }
            }else if (element instanceof XWPFTable) {
                String heading = ((XWPFTable)element).getRow(0).getCell(0).getText().trim();
                if (headingFound || (onlyMainHeading && mainHeadingFound)) {
                    tableCount++;
                    if(tableCount == tablePosition) {
                        table = (XWPFTable) element;
                        break;
                    }
                }else if(mainHeadingFound && heading.equals(targetHeading)){
                    headingFound = true;
                }else if(heading.equals(targetMainHeading)){
                    mainHeadingFound = true;
                }
            }
        }
        if (onlyMainHeading && !mainHeadingFound) {
            throw new NoSuchElementException("Heading not found in the document.");
        }
        if(tablePosition != tableCount){
            throw new InvalidParameterException("Invalid table position ." + tableCount);
        }
        return table;
    }


    public int getRowIndex(
            XWPFTable table,
            String columnIdentifier,
            String rowIdentifier
    ){
       int rowIndex = -1;
       int columnIndex = getHeaderIndex(table.getRow(0),columnIdentifier);
       if(columnIndex == -1){
           return rowIndex;
       }
       for(int index = 0;index < table.getNumberOfRows();index++) {
           if (table.getRow(index).getCell(columnIndex).getText().trim().equals(rowIdentifier)) {
               rowIndex = index;
               break;
           }
       }
       return rowIndex;
    }
    public boolean tableCellEditor(
            XWPFTableCell cell,
            String operation,
            String data
    ){
        boolean result = true;
        if(cell != null){
            switch (operation){
                case "Clear":
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        for (XWPFRun run : paragraph.getRuns()) {
                            run.setText("", 0);
                        }
                    }
                    break;
                case "Modify":
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        for (XWPFRun run : paragraph.getRuns()) {
                            run.setText("", 0);
                        }
                    }
                    cell.setText(data);
                    break;
                case "Append":
                    XWPFParagraph lastParagraph = null;
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        lastParagraph = paragraph;
                    }
                    if (lastParagraph != null) {
                        lastParagraph.createRun().setText(" "+data);
                    } else {
                        lastParagraph = cell.addParagraph();
                        lastParagraph.createRun().setText(" "+data);
                    }
                    break;
                case "Verify":
                    if(!cell.getText().trim().equals(data)){
                        result = false;
                    }
                    break;
            }
        }else{
            throw new NoSuchElementException("Target cell not found");
        }
        return result;
    }
    public XWPFTableCell getTableCell(
            XWPFTable table,
            String changeColumnIdentifier,
            String columnIdentifier,
            String rowIdentifier
    ) {
        XWPFTableRow headerRow = table.getRow(0);
        XWPFTableCell targetCell = null;
        int columnIndex = getHeaderIndex(headerRow, columnIdentifier);
        int changeColumnIndex = getHeaderIndex(headerRow, changeColumnIdentifier);
        if(columnIndex == -1 || changeColumnIndex == -1){
            throw new RuntimeException("Specified cell not found");
        }
        for(XWPFTableRow row : table.getRows()){
            if(row.getCell(columnIndex).getText().trim().equals(rowIdentifier)){
                targetCell = row.getCell(changeColumnIndex);
            }
        }
        return targetCell;
    }
    private static int getHeaderIndex(XWPFTableRow headerRow, String header){
        for(int index = 0;index < headerRow.getTableCells().size();index++){
            if(headerRow.getCell(index).getText().trim().equals(header)){
                return index;
            }
        }
        return -1;
    }
}
