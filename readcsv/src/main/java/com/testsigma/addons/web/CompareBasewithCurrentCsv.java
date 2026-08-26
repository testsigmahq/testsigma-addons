package com.testsigma.addons.web;

import com.opencsv.CSVReader;
import com.testsigma.addons.web.util.FileDownloadUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;

@Data
@Action(actionText = "Compare headers of current CSV file with dir1 with the base CSV file with dir2",
        description = "Compares the CSV of both current and old csv file",
        applicationType = ApplicationType.WEB)
public class CompareBasewithCurrentCsv extends WebAction {

  @TestData(reference = "dir1")
  private com.testsigma.sdk.TestData dir1;

  @TestData(reference = "dir2")
  private com.testsigma.sdk.TestData dir2;

  @Override
  public com.testsigma.sdk.Result execute() {
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

    String dir1Path = dir1 != null && dir1.getValue() != null ? dir1.getValue().toString() : null;
    String dir2Path = dir2 != null && dir2.getValue() != null ? dir2.getValue().toString() : null;

    logger.info("dir1 (current CSV folder) resolved to: " + dir1Path);
    logger.info("dir2 (base CSV file) resolved to: " + dir2Path);

    Reader reader1 = null;
    Reader reader2 = null;
    CSVReader csvreader = null;
    CSVReader csvreader2 = null;
    File downloadedFile1 = null;
    File downloadedFile2 = null;

    try {
      // --- Resolve dir1: a local directory (pick latest file), a local file, or a Testsigma upload URL ---
      File s1 = FileDownloadUtil.resolveInputFile(dir1Path, "dir1");
      if (FileDownloadUtil.isUrl(dir1Path)) {
        downloadedFile1 = s1;
      }
      logger.info("File picked for dir1 (current CSV): " + s1.getAbsolutePath());

      // --- Resolve dir2: a local file or a Testsigma upload URL ---
      File s2 = FileDownloadUtil.resolveInputFile(dir2Path, "dir2");
      if (FileDownloadUtil.isUrl(dir2Path)) {
        downloadedFile2 = s2;
      }
      logger.info("File picked for dir2 (base CSV): " + s2.getAbsolutePath());

      // --- Read and compare headers ---
      reader1 = new FileReader(s1.getAbsolutePath());
      csvreader = new CSVReader(reader1);

      reader2 = new FileReader(s2.getAbsolutePath());
      csvreader2 = new CSVReader(reader2);

      String[] header1 = csvreader.readNext();
      String[] header2 = csvreader2.readNext();

      logger.info("Header from current CSV (dir1): " + Arrays.toString(header1));
      logger.info("Header from base CSV (dir2): " + Arrays.toString(header2));

      if (header1 == null || header2 == null) {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage("Could not read header row. Current CSV (dir1) header: "
                + Arrays.toString(header1) + " | Base CSV (dir2) header: " + Arrays.toString(header2)
                + ". One or both files appear to be empty.");
        return result;
      }

      if (Arrays.equals(header1, header2)) {
        setSuccessMessage("The headers of the two files are the same");
        System.out.println("The headers of the two files are the same.");
      } else {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage("Header of both the files do not match. Current: "
                + Arrays.toString(header1) + " | Base: " + Arrays.toString(header2));
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
      FileDownloadUtil.closeQuietly(csvreader2);
      FileDownloadUtil.closeQuietly(reader2);
      FileDownloadUtil.deleteQuietly(downloadedFile1);
      FileDownloadUtil.deleteQuietly(downloadedFile2);
    }
    return result;
  }
}
