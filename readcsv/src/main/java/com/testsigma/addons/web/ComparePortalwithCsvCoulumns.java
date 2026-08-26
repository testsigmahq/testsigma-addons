package com.testsigma.addons.web;

import com.opencsv.CSVReader;
import com.testsigma.addons.web.util.FileDownloadUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Compare column locator with CSV file with directory where column index is testdata",
        description = "Stores the column and compares the same with csv file having index",
        applicationType = ApplicationType.WEB)
public class ComparePortalwithCsvCoulumns extends WebAction {

    @TestData(reference = "directory")
    private com.testsigma.sdk.TestData dir1;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData index;

    @Element(reference = "locator")
    private com.testsigma.sdk.Element element;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {

        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        String dir1Path = dir1 != null && dir1.getValue() != null ? dir1.getValue().toString() : null;
        logger.info("directory resolved to: " + dir1Path);

        File downloadedFile1 = null;
        Reader reader1 = null;
        CSVReader csvreader = null;

        try {
            // getting list from webpage
            List<String> webpageElements = new ArrayList<>();
            By xpath = element.getBy();
            List<WebElement> elements = driver.findElements(xpath);
            for (WebElement el : elements) {
                String text = el.getText().trim();
                if (!text.isEmpty()) {
                    webpageElements.add(text);
                }
            }

            // Read column from CSV file
            List<String> csvColumn = new ArrayList<>();
            File s1 = FileDownloadUtil.resolveInputFile(dir1Path, "directory");
            if (FileDownloadUtil.isUrl(dir1Path)) {
                downloadedFile1 = s1;
            }
            logger.info("File picked for directory: " + s1.getAbsolutePath());

            reader1 = new FileReader(s1.getAbsolutePath());
            csvreader = new CSVReader(reader1);
            List<String[]> rows = csvreader.readAll();
            int columnIndex = Integer.valueOf(index.getValue().toString());
            for (int i = 1; i < rows.size(); i++) { // Skip header row
                csvColumn.add(rows.get(i)[columnIndex]);
            }

            logger.info("Portal elements: " + webpageElements);
            logger.info("CSV column values: " + csvColumn);

            if (csvColumn.equals(webpageElements)) {
                setSuccessMessage("Data matched with portal");
            } else {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Data does not match with portal. CSV column: " + csvColumn
                        + " | Portal elements: " + webpageElements);
            }
        } catch (FileDownloadUtil.InvalidTestDataException e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(e.getMessage());
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Data does not match with portal " + e.getMessage());
            logger.debug(e.getMessage());
        } finally {
            FileDownloadUtil.closeQuietly(csvreader);
            FileDownloadUtil.closeQuietly(reader1);
            FileDownloadUtil.deleteQuietly(downloadedFile1);
        }
        return result;
    }
}
