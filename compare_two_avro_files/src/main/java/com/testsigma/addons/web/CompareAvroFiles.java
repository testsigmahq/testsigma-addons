package com.testsigma.addons.web;

import com.opencsv.CSVWriter;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Verify that the data in avro file1 test-data1 and avro file2 test-data2 is equal",
        description = "Verifies that the two avro files data is same",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class CompareAvroFiles extends WebAction {

  @TestData(reference = "test-data1")
  private com.testsigma.sdk.TestData file1;

  @TestData(reference = "test-data2")
  private com.testsigma.sdk.TestData file2;

  @Override
  public Result execute() throws NoSuchElementException {
    Result result = Result.SUCCESS;
    try {
      File avroFile1 = urlToFileConverter("avrofile1.avro", file1.getValue().toString());
      File avroFile2 = urlToFileConverter("avrofile2.avro", file2.getValue().toString());

      File csvFile1 = File.createTempFile("csvfile1",".csv");
      File csvFile2 = File.createTempFile("csvfile2",".csv");

      logger.info("Converting avrofile1 to csv");
      convertAvroToCsv(avroFile1, csvFile1);
      logger.info("Converted");

      logger.info("Converting avrofile2 to csv");
      convertAvroToCsv(avroFile2, csvFile2);
      logger.info("Converted");

      List<String[]> file1Data = readCSV(csvFile1);
      List<String[]> file2Data = readCSV(csvFile2);

      boolean comparisonResult  = compareCSV(file1Data, file2Data);

      if (comparisonResult) {
        setSuccessMessage("Successfully verified that both Avro files data is same");
      } else {
        setErrorMessage("Both avro files data is not same");
        result = Result.FAILED;
      }

    } catch (IOException e) {
      logger.info("Exception occurred: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Unable to compare both the avro files");
      result = Result.FAILED;
    }
    return result;
  }

  private File urlToFileConverter(String fileName, String s3url) throws IOException {
    logger.info("File name:" + fileName);
    URL urlObject = new URL(s3url);
    File tempFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
    FileUtils.copyURLToFile(urlObject,tempFile);
    logger.info("Temp file created with name" + tempFile.getName() + " at path " + tempFile.getAbsolutePath());
    return tempFile;
  }

  private void convertAvroToCsv(File avroFilePath, File csvFilePath) throws IOException {
    try (DataFileStream<GenericRecord> dataFileReader = new DataFileStream<>(new FileInputStream(avroFilePath), new GenericDatumReader<>())) {
      try (CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFilePath))) {

        Schema schema = dataFileReader.getSchema();
        String[] header = new String[schema.getFields().size()];
        int i = 0;
        for (Schema.Field field : schema.getFields()) {
          header[i++] = field.name();
        }
        csvWriter.writeNext(header);

        for (GenericRecord record : dataFileReader) {
          String[] row = new String[header.length];
          for (i = 0; i < header.length; i++) {
            Object value = record.get(i);
            row[i] = value instanceof Utf8 ? value.toString() : String.valueOf(value);
          }
          csvWriter.writeNext(row);
        }
      }
    }
  }

  private List<String[]> readCSV(File csvFile) throws IOException {
    logger.info("Reading the csv file");
    List<String[]> data = new ArrayList<>();
    BufferedReader reader = new BufferedReader(new FileReader(csvFile));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] row = line.split(",");
      data.add(row);
    }
    reader.close();
    logger.info("Reading completed");
    return data;
  }
  private boolean compareCSV(List<String[]> data1, List<String[]> data2) {
    logger.info("Comparison started..");
    int rows = Math.min(data1.size(), data2.size());
    int columns = Math.min(data1.get(0).length, data2.get(0).length);
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        if (!data1.get(i)[j].equals(data2.get(i)[j])) {
          return false;
        }
      }
    }
    return true;
  }
}