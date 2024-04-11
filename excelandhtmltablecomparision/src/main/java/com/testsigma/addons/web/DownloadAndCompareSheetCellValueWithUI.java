package com.testsigma.addons.web;


import org.openqa.selenium.NoSuchElementException;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.MailboxMessage;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.Mailbox;
import com.testsigma.sdk.annotation.TestData;

import lombok.Data;

import java.net.URL;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;


@Data
@Action(actionText = "Verify the element-locator value <option> the data in column column-name of row row-number from latest mail-box alias message file hyperlink of hypertext",
        description = "Compares the specified element value with the cell value matching to the column name & row number of XLSX or CSV donwloaded from mail box alias latest message hyperlink with specified text",
        applicationType = ApplicationType.WEB)
public class DownloadAndCompareSheetCellValueWithUI extends WebAction  {

    private static final String NO_MAIL_MESSAGE = "Mail box does not contain any mails";
    private static final String HYPERLINK_START_NOTATION = " (";
    private static final String HYPERLINK_END_NOTATION = ")";

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "<option>", allowedValues = {"Equals", "Contains"})
    private com.testsigma.sdk.TestData option;

    @TestData(reference = "column-name")
    private com.testsigma.sdk.TestData columnName;

    @TestData(reference = "row-number")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "mail-box", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData mail;

    @TestData(reference = "hypertext")
    private com.testsigma.sdk.TestData hypertext;

    @Mailbox
    private com.testsigma.sdk.Mailbox mailbox;

    @Override
    protected Result execute() throws NoSuchElementException {
        

        try {
            Utilities utilities = new Utilities(driver,logger);
            
            mailbox.setEmail(mail.getValue().toString());
            List<MailboxMessage> messageList = mailbox.getMessages();

            if(messageList == null || messageList.isEmpty()){
                setErrorMessage(NO_MAIL_MESSAGE);
                return Result.FAILED;
            }

            int lastIndex = messageList.size() - 1;
            MailboxMessage latestMailBoxMessage = messageList.get(lastIndex);

            // logger.info("latestMailBoxMessage:" + latestMailBoxMessage.getTextMessage());

            String hypertextValue = hypertext.getValue().toString();
            String searchText = hypertextValue + HYPERLINK_START_NOTATION;

            int startIndex = latestMailBoxMessage.getTextMessage().indexOf(searchText);
            if( startIndex == -1){
                throw new Exception(String.format("Hyperlink with text %s not found", hypertextValue));
            }

            String substring = latestMailBoxMessage.getTextMessage().substring(startIndex + searchText.length());
            int endIndex = substring.indexOf(HYPERLINK_END_NOTATION);

            if( endIndex == -1){
                throw new Exception(String.format("Hyperlink with text %s not found", hypertextValue));
            }
            logger.info(String.format("Start : %d, End : %d", startIndex, endIndex));

            substring = substring.substring(0, endIndex);
            logger.info(String.format("Substring : %s ", substring));

            if( substring == null){
                throw new Exception(String.format("Hyperlink with text %s not found", hypertextValue));
            }

            String attachmentS3Url = substring.trim();
            logger.info(String.format("Attachement URL : %s", attachmentS3Url));
            attachmentS3Url = attachmentS3Url.replaceAll("&amp;", "&");
            
            URL presignedUrl = new URL(attachmentS3Url);
            logger.info(String.format("Presigned URL : %s", presignedUrl));

            try {
                String selectedOption = option.getValue().toString();
                String elementValue = element.getElement().getText().toString();
                String sheetColumnName = columnName.getValue().toString();
                int sheetRowNumber = Integer.parseInt(rowNumber.getValue().toString());

                String sheetCellValue = null;
                
        
                String loggerMessage = String.format("Element Value: %s, Excel Column Name: %s, Excel Row Number: %d, Selected Option : %s", elementValue, sheetColumnName, sheetRowNumber, selectedOption);
                System.out.println(loggerMessage);
                logger.info(loggerMessage);
            
                if(attachmentS3Url.contains(".xlsx")){
                    loggerMessage = String.format("Reading XLSX file from url %s", presignedUrl);
                    logger.info(loggerMessage);
                    sheetCellValue = utilities.readXLSXCellValue(presignedUrl, sheetColumnName, sheetRowNumber);
                } else if (attachmentS3Url.contains(".csv")) {
                    loggerMessage = String.format("Reading CSV file from url %s", presignedUrl);
                    logger.info(loggerMessage);
                    sheetCellValue = utilities.readCSVCellValue(presignedUrl, sheetColumnName, sheetRowNumber);
                } else {
                    throw new Exception("Only CSV & XLSX files are supported..!");
                }

                if(sheetCellValue == null) {
                    loggerMessage = String.format("Not Able to fetch the sheet cell value of Row Number : %d and Cloumn Name : %s", sheetRowNumber, sheetColumnName);
                    logger.info(loggerMessage);
                    throw new Exception(loggerMessage);
                }

                loggerMessage = String.format("Sheet Cell Value : %s", sheetCellValue);
                logger.info(loggerMessage);

                if(selectedOption.equals("Equals")){
                    if (!elementValue.trim().equals(sheetCellValue.trim())) {
                        loggerMessage = String.format("Element value : %s is not equal to cell value : %s", elementValue, sheetCellValue);
                        setErrorMessage(loggerMessage);
                        return Result.FAILED;
                    } else {
                        loggerMessage = String.format("Element value : %s is equal to cell value : %s", elementValue, sheetCellValue);
                        setSuccessMessage(loggerMessage);
                        return Result.SUCCESS;
                    }
                } else {
                    if (!elementValue.trim().contains(sheetCellValue.trim())) {
                        loggerMessage = String.format("Element value : %s is not present in cell value : %s",  elementValue, sheetCellValue);
                        setErrorMessage(loggerMessage);
                        return Result.FAILED;
                    } else {
                        loggerMessage = String.format("Element value : %s is present in cell value : %s", elementValue, sheetCellValue);
                        setSuccessMessage(loggerMessage);
                        return Result.SUCCESS;
                    }
                }

            } catch(FileNotFoundException ex) {
                setErrorMessage("File Not found : "+ ex.getMessage());
                logger.info("File Not found : "+ ex.getMessage());
                ex.printStackTrace();
                return Result.FAILED;
            } catch(IOException ex) {
                setErrorMessage("Failed to read file : "+ ex.getMessage());
                logger.info("Failed to read file : "+ ex.getMessage());
                ex.printStackTrace();
                return Result.FAILED;
            }
        } catch (Exception ex) {
            setErrorMessage(ex.getMessage());
            logger.info(ex.getMessage());
            ex.printStackTrace();
            return Result.FAILED;
        }
    }      
}

