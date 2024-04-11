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
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;


@Data
@Action(actionText = "Verify the element-locator value <option> the data in column column-name of row row-number from latest mailbox mail-box message attachment",
        description = "Compares the specified element value with the cell value matching to the column name & row number of XLSX or CSV attachement from mail box alias latest message",
        applicationType = ApplicationType.WEB)
public class CompareCellValueWithUIElement extends WebAction  {

    private static final String NO_MAIL_MESSAGE = "Mail box does not contain any mails";
    private static final String NO_ATTACHMENTS = "Latest mail doesn't contain any attachments";

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
    
            logger.info("latestMailBoxMessage HTMl:" + latestMailBoxMessage.getHtmlMessage());
            logger.info("latestMailBoxMessage:" + latestMailBoxMessage.getTextMessage());

            Map<String ,String> attachments = latestMailBoxMessage.getAttachmentUrls();

            if(attachments == null || attachments.isEmpty()){
                setErrorMessage(NO_ATTACHMENTS);
                return Result.FAILED;
            }

            for (String attachmentName : attachments.keySet()) { 
                String attachmentS3Url = attachments.get(attachmentName);
                URL presignedUrl = new URL(attachmentS3Url);
                try {
                    String selectedOption = option.getValue().toString();
                    String elementValue = element.getElement().getText().toString();
                    String sheetColumnName = columnName.getValue().toString();
                    int sheetRowNumber = Integer.parseInt(rowNumber.getValue().toString());

                    String sheetCellValue = null;
                    
            
                    String loggerMessage = String.format("Element Value: %s, Excel Column Name: %s, Excel Row Number: %d, Selected Option : %s", elementValue, sheetColumnName, sheetRowNumber, selectedOption);
                    System.out.println(loggerMessage);
                    logger.info(loggerMessage);
                
                    if(attachmentName.contains(".xlsx")){
                        sheetCellValue = utilities.readXLSXCellValue(presignedUrl, sheetColumnName, sheetRowNumber);
                    } else if (attachmentName.contains(".csv")) {
                        sheetCellValue = utilities.readCSVCellValue(presignedUrl, sheetColumnName, sheetRowNumber);
                    } else {
                        throw new Exception("Only CSV & XLSX files are supported..!");
                    }

                    if(sheetCellValue == null) {
                        loggerMessage = String.format("Not Able to fetch the sheet cell value of Row Number : %d and Cloumn Name : %s", sheetColumnName, sheetRowNumber);
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
                
            }

            setErrorMessage("No XLSX & CSV Files found in mail attachements");
            return Result.SUCCESS;
        } catch (Exception ex) {
            setErrorMessage(ex.getMessage());
            logger.info(ex.getMessage());
            ex.printStackTrace();
            return Result.FAILED;
        }
    }      
}

