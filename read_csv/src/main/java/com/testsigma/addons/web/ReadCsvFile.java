package com.testsigma.addons.web;

import java.io.FileReader;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.opencsv.*;
import lombok.Data;

@Data
@Action(actionText = "Testing Read column testdata1 and row testdata2 where absolutepath is testdata3 and store in runtime variable",
		description = "This addon will read the data from CSV file and store in runtime variable",
		applicationType = ApplicationType.WEB)

public class ReadCsvFile extends WebAction
{
	@TestData(reference = "testdata1")
	private com.testsigma.sdk.TestData col;

	@TestData(reference = "testdata2")
	private com.testsigma.sdk.TestData row;

	@TestData(reference = "testdata3")
	private com.testsigma.sdk.TestData filepath_;

	@TestData(reference = "variable", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData runtimeVar;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public Result execute() throws NoSuchElementException
	{
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		try {

			logger.debug("Column = " + col.getValue().toString());
			logger.debug("Row = " + row.getValue().toString());
			logger.debug("testdata = " + filepath_.getValue().toString());
			logger.debug("variable = " + runtimeVar.getValue().toString());

			String filePath = filepath_.getValue().toString();
			int colToRead = Integer.parseInt(col.getValue().toString());
			int rowToRead = Integer.parseInt(row.getValue().toString());

			CSVReader csvread = new CSVReader(new FileReader(filePath));
			List<String[]> csvvalue = csvread.readAll();

			if (rowToRead < 0 || rowToRead >= csvvalue.size()) {
				setErrorMessage("Invalid row number. Row number should be between 1 and " + csvvalue.size());
				return Result.FAILED;
			}

			String[] csvRow = csvvalue.get(rowToRead);

			if (colToRead < 0 || colToRead >= csvRow.length) {
				setErrorMessage("Invalid column number. Column number should be between 1 and " + csvRow.length);
				return Result.FAILED;
			}

			String csvColumn = csvRow[colToRead];

			logger.info("Value stored in " + runtimeVar.getValue().toString() + " is " + csvColumn);
			runTimeData.setKey(runtimeVar.getValue().toString());
			runTimeData.setValue(csvColumn);
			setSuccessMessage("Successfully Read the data from Row : " + rowToRead + " column : " + colToRead + " and stored in runtime variable " + runtimeVar.getValue().toString() + " = " + csvColumn);
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid number format provided for row or column. Please provide valid integer values.");
			logger.warn("Error during number parsing " + e);
			return Result.FAILED;
		}
		catch (Exception e) {
			setErrorMessage("An error occurred while reading the CSV file: " + e.getMessage());
			logger.warn("Error during CSV processing " + ExceptionUtils.getStackTrace(e));
			return Result.FAILED;
		}

		return result;
	}

}