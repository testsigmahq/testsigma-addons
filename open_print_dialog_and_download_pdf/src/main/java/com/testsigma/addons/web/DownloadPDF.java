package com.cyclelove.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.TracedCommandExecutor;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

@Data
@Action(actionText = "Open Print dialog, download the pdf in the print dialog and store the downloaded pdf path in runtime" +
        " variable variable-name and close the print dialog window",
        description = "Downloads the pdf present in print dialog and stores that downloaded pdf file path" +
                " in runtime variable and closes the print dialog window",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class DownloadPDF extends WebAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variable_;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;


    @Override
    public Result execute() throws NoSuchElementException {
        // Your Awesome code starts here
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;
        String variable = variable_.getValue().toString();
        String localMachineDownloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
        File latestDownloadedFile;
        String currentWindowHandle = driver.getWindowHandle();
        String targetHandle = null;
        logger.info("Current window handle: " + currentWindowHandle);
        try {
            targetHandle = switchToPrintDialog();
            Thread.sleep(3000);
            String fileName = getUniqueFileName();
            if (isCloudExecution()) {
                JavascriptExecutor js = (JavascriptExecutor) driver;

                logger.info("First enter..");
                js.executeScript("lambda-perform-keyboard-events:{Enter}");
                Thread.sleep(10000);

                logger.info("Naming the file..");
                js.executeScript("lambda-perform-keyboard-events:" + fileName);
                Thread.sleep(2000);

                logger.info("Done, second enter...");
                js.executeScript("lambda-perform-keyboard-events:{Enter}");
                Thread.sleep(2000);

                logger.info("Done, fetching file if it downloads");
                String base64code = getBase64CodeOfDownloadedFileFromCloud();
                latestDownloadedFile = File.createTempFile("output",".pdf");
                logger.info("Retrieved base64 code, converting to pdf");
                decodeBase64ToPDF(base64code, latestDownloadedFile.getAbsolutePath());
                logger.info("Base64 to pdf converted successfully");
            } else {
                Robot robot = new Robot();

                logger.info("First enter..");
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                Thread.sleep(2000);

                logger.info("Done, second enter...");
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                Thread.sleep(2000);

                logger.info("Done, fetching file if it downloads");
                latestDownloadedFile = getLatestFileFromDirectory(localMachineDownloadsPath);
            }
            if (latestDownloadedFile != null &&
                    latestDownloadedFile.exists() &&
                    latestDownloadedFile.getName().endsWith(".pdf")
            ) {
                logger.info("Setting the data to runtime");
                runTimeData.setKey(variable);
                runTimeData.setValue(latestDownloadedFile.getAbsolutePath());
                setSuccessMessage(String.format("Successfully downloaded the file and stored the file path " +
                        "<b>%s</b> in runtime variable <b>%s</b>", latestDownloadedFile.getAbsolutePath(), variable));
            } else {
                logger.info(latestDownloadedFile != null ? "file is " + latestDownloadedFile.getAbsolutePath() : "file is null");
                setErrorMessage("Failed to download and fetch that file path");
                result = Result.FAILED;
            }
        } catch (RuntimeException e) {
            result = Result.FAILED;
            logger.info("Runtime exception: " + ExceptionUtils.getMessage(e));
        } catch (Exception e) {
            result = Result.FAILED;
            logger.info("Exception occurred " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to perform the operation");
        } finally {
            if (targetHandle != null && driver.getWindowHandles().contains(targetHandle)) {
                driver.close();
            }
            driver.switchTo().window(currentWindowHandle);
            logger.info("Window handles at the end of execution: " + driver.getWindowHandles());
        }
        return result;
    }

    private boolean isCloudExecution() {
        RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
        String remoteAddress;
        try {
            // Get the command executor and check its type
            HttpCommandExecutor httpExecutor;
            if (remoteDriver.getCommandExecutor() instanceof TracedCommandExecutor) {
                // If it's a TracedCommandExecutor, get the delegate field
                TracedCommandExecutor tracedExecutor = (TracedCommandExecutor) remoteDriver.getCommandExecutor();
                Field delegateField = TracedCommandExecutor.class.getDeclaredField("delegate");
                delegateField.setAccessible(true); // Allow access to the private field
                httpExecutor = (HttpCommandExecutor) delegateField.get(tracedExecutor);
            } else if (remoteDriver.getCommandExecutor() instanceof HttpCommandExecutor) {
                // If it's directly an HttpCommandExecutor
                httpExecutor = (HttpCommandExecutor) remoteDriver.getCommandExecutor();
            } else {
                logger.info("Unsupported CommandExecutor type");
                setErrorMessage("Unable to initiate the operation");
                throw new RuntimeException("Unsupported CommandExecutor type");
            }

            // Retrieve the remote address from the executor
            remoteAddress = httpExecutor.getAddressOfRemoteServer().toString();
            if (remoteAddress.toLowerCase().contains("lambdatest")) {
                return true;
            } else {
                logger.info("Unsupported remote address type");
                setErrorMessage("Unable to initiate the operation");
                throw new RuntimeException("Unsupported remote address type");
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            logger.info("Unable to retrieve remote address");
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to initiate the operation");
            throw new RuntimeException("Couldn't retrieve remote address");
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private File getLatestFileFromDirectory(String dirPath) {
        try {
            Path dir = Paths.get(dirPath);
            Optional<Path> latestFilePath = Files.list(dir)
                    .filter(Files::isRegularFile)  // Ensure we only consider regular files
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

            return latestFilePath.map(Path::toFile).orElse(null);
        } catch (Exception e) {
            setErrorMessage("Unable to retrieve the downloaded file path");
            logger.info("Exception at retrieval : " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("File path retrieval failed");
        }
    }

    private String switchToPrintDialog() {
        try {
            // Trigger the print dialog using JavaScript
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("setTimeout(function() { window.print(); }, 1000);");
            logger.info("Triggered the print dialog");

            // Wait for a short period to allow the print dialog to open
            Thread.sleep(10000);

            // Get the current window handles
            Set<String> handles = driver.getWindowHandles();
            logger.info("Window handles after triggering print dialog: " + handles);
            List<String> windowsList = new ArrayList<>(handles);

            // Ensure there is a new window for the print dialog
            if (windowsList.size() < 2) {
                throw new RuntimeException("Print dialog did not open");
            }

            String targetHandle = windowsList.get(windowsList.size() - 1); // Get the last window (print dialog)

            // Check if the target window is still available in the list of window handles
            if (!driver.getWindowHandles().contains(targetHandle)) {
                throw new RuntimeException("Target window already closed");
            }

            driver.switchTo().window(targetHandle); // Switch to the print dialog
            logger.info("Switched to the print dialog");
            return targetHandle;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setErrorMessage("Thread interrupted while waiting for print dialog");
            throw new RuntimeException("Interrupted during print dialog setup");
        } catch (Exception e) {
            setErrorMessage("Unable to switch to print dialog, make sure a print dialog is opened to perform the operation");
            throw new RuntimeException("No print dialog opened");
        }
    }


    private String getBase64CodeOfDownloadedFileFromCloud() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            List<String> filesInDownloads = (List<String>) js.executeScript("lambda-file-list=pdf");
            logger.info("Files in downloads : " + filesInDownloads);
            String fileName;
            if (!filesInDownloads.isEmpty()) {
                fileName = filesInDownloads.get(filesInDownloads.size() - 1);
                logger.info("File name to retrieve: " + fileName);
                String script = "lambda-file-content=" + fileName;
                return js.executeScript(script).toString();
            } else {
                setErrorMessage("Unable to retrieve the downloaded file path");
                throw new RuntimeException("File path retrieval failed");
            }

        } catch (Exception e) {
            setErrorMessage("Unable to retrieve the downloaded file path from remote device");
            logger.info("Exception at retrieval : " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("File path retrieval failed");
        }
    }

    public void decodeBase64ToPDF(String base64Str, String outputFilePath) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Str);
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(decodedBytes);
        } catch (IOException e) {
            logger.info("Exception occurred while decoding : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to retrieve the downloaded file path");
            throw new RuntimeException("Decoding error");
        }
    }

    private String getUniqueFileName() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(now);
        String baseFileName = "file";
        return baseFileName + timestamp + ".pdf";
    }
}
