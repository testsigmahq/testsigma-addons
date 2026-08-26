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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Action(actionText = "Compare headers of current CSV file with dir1 with the portal headers elements",
        description = "Compares headers of current csv with portal headers",
        applicationType = ApplicationType.WEB)
public class CompareHeadersFromPortalWithCsv extends WebAction {

    @TestData(reference = "dir1")
    private com.testsigma.sdk.TestData dir1;

    @Element(reference = "elements")
    private com.testsigma.sdk.Element element;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {

        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        String dir1Path = dir1 != null && dir1.getValue() != null ? dir1.getValue().toString() : null;
        logger.info("dir1 resolved to: " + dir1Path);

        Reader reader1 = null;
        CSVReader csvreader = null;
        File downloadedFile1 = null;

        try {
            File s1 = FileDownloadUtil.resolveInputFile(dir1Path, "dir1");
            if (FileDownloadUtil.isUrl(dir1Path)) {
                downloadedFile1 = s1;
            }
            logger.info("File picked for dir1 (current CSV): " + s1.getAbsolutePath());

            reader1 = new FileReader(s1.getAbsolutePath());
            csvreader = new CSVReader(reader1);

            String[] header1 = csvreader.readNext();
            if (header1 == null) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Could not read header row from dir1 CSV. The file appears to be empty.");
                return result;
            }
            List<String> headerList = Arrays.asList(header1);

            List<String> webpageElements = new ArrayList<>();
            By xpath = element.getBy();
            List<WebElement> elements = driver.findElements(xpath);
            for (WebElement el : elements) {
                String text = el.getText().trim().toLowerCase();
                if (!text.isEmpty()) {
                    webpageElements.add(text);
                }
            }

            logger.info("Header one " + headerList);
            logger.info("List of element " + webpageElements);

            List<String> lowerHeaderList = headerList.stream().map(String::toLowerCase).collect(Collectors.toList());

            if (lowerHeaderList.equals(webpageElements)) {
                setSuccessMessage("The headers and portal details matches");
                System.out.println("The headers of the two files are the same.");
            } else {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Header of both the files do not match. CSV header: " + lowerHeaderList
                        + " | Portal elements: " + webpageElements);
                System.out.println("The headers of the two files are different.");
            }
        } catch (FileDownloadUtil.InvalidTestDataException e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(e.getMessage());
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Operation Failed " + e.getMessage());
            logger.warn("Exception Occurred: " + e);
        } finally {
            FileDownloadUtil.closeQuietly(csvreader);
            FileDownloadUtil.closeQuietly(reader1);
            FileDownloadUtil.deleteQuietly(downloadedFile1);
        }
        return result;
    }
}
