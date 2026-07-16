package com.testsigma.addons.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Convert the date from the testdata in format1 to format2 and store it in a runtime variable variable-name", description = "To store the number of char from testdata into runtime variable", applicationType = ApplicationType.ANDROID)

public class DateFormatConversion extends AndroidAction {

	@TestData(reference = "testdata")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "format1")
	private com.testsigma.sdk.TestData testData2;

	@TestData(reference = "format2")
	private com.testsigma.sdk.TestData testData3;

	@TestData(reference = "variable-name", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData runtimeVar;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public Result execute() {

		logger.info("Initiating execution");

		Result result = Result.SUCCESS;

		try {

			if (testData1.getValue() == null || testData2.getValue() == null || testData3.getValue() == null
					|| runtimeVar.getValue() == null) {

				logger.warn("One or more inputs are null.");
				setErrorMessage("One or more required inputs are null.");
				return Result.FAILED;
			}

			String date = testData1.getValue().toString().trim();

			// Convert non-Java day patterns (ddd, dddd, ddddd...) to EEEE
			String inputFormatPattern = convertToJavaDatePattern(testData2.getValue().toString().trim());

			String outputFormatPattern = convertToJavaDatePattern(testData3.getValue().toString().trim());

			String runtimeVariable = runtimeVar.getValue().toString().trim();

			// Validate inputs
			if (date.isEmpty() || inputFormatPattern.isEmpty() || outputFormatPattern.isEmpty()
					|| runtimeVariable.isEmpty()) {

				logger.warn("One or more required inputs are empty.");
				setErrorMessage("One or more required inputs are empty.");
				return Result.FAILED;
			}

			String validationMessage = validateConversion(inputFormatPattern, outputFormatPattern);

			if (validationMessage != null) {
				logger.warn(validationMessage);
				setErrorMessage(validationMessage);
				return Result.FAILED;
			}

			logger.info("Input Date      : " + date);
			logger.info("Input Format    : " + inputFormatPattern);
			logger.info("Output Format   : " + outputFormatPattern);
			logger.info("Runtime Variable: " + runtimeVariable);
			
			SimpleDateFormat inputFormat = new SimpleDateFormat(inputFormatPattern);
			inputFormat.setLenient(false);

			SimpleDateFormat outputFormat = new SimpleDateFormat(outputFormatPattern);

			Date parsedDate = inputFormat.parse(date);

			String convertedDate = outputFormat.format(parsedDate);

			runTimeData.setKey(runtimeVariable);
			runTimeData.setValue(convertedDate);

			logger.info("Converted Date  : " + convertedDate);

			setSuccessMessage("Successfully converted date from '" + inputFormatPattern + "' to '" + outputFormatPattern
					+ "' and stored the value '" + convertedDate + "' in runtime variable '" + runtimeVariable + "'.");

		} catch (ParseException e) {

			logger.warn("Failed to parse date: " + e.getMessage());

			setErrorMessage("Invalid date or date format. Please verify the input date and format patterns. Error: "
					+ e.getMessage());

			result = Result.FAILED;

		} catch (Exception e) {
			logger.warn("Unexpected error occurred: " + ExceptionUtils.getStackTrace(e));
			setErrorMessage("Operation failed. Error: " + ExceptionUtils.getMessage(e));
			result = Result.FAILED;
		}

		return result;
	}

	private String convertToJavaDatePattern(String pattern) {
		return pattern.replaceAll("d{4,}", "EEEE").replaceAll("d{3}", "EEE");
	}

	private String validateConversion(String inputPattern, String outputPattern) {

		if (outputPattern.contains("y") && !inputPattern.contains("y")) {
			return "Invalid conversion. The input format does not contain a year, so it cannot be converted to a year-based output.";
		}

		if (outputPattern.contains("M") && !inputPattern.contains("M")) {
			return "Invalid conversion. The input format does not contain a month, so it cannot be converted to a month-based output.";
		}

		if (outputPattern.contains("d") && !inputPattern.contains("d")) {
			return "Invalid conversion. The input format does not contain a day of month, so it cannot be converted to a day-based output.";
		}

		if (outputPattern.contains("E")
				&& !(inputPattern.contains("E")
				|| (inputPattern.contains("d")
				&& inputPattern.contains("M")
				&& inputPattern.contains("y")))) {

			return "Invalid conversion. The input format must contain either the day of the week or the day, month, and year to determine the day of the week.";
		}

		if ((outputPattern.contains("H") || outputPattern.contains("h")
				|| outputPattern.contains("k") || outputPattern.contains("K"))
				&& !(inputPattern.contains("H") || inputPattern.contains("h")
				|| inputPattern.contains("k") || inputPattern.contains("K"))) {
			return "Invalid conversion. The input format does not contain an hour, so it cannot be converted to an hour-based output.";
		}

		if (outputPattern.contains("m") && !inputPattern.contains("m")) {
			return "Invalid conversion. The input format does not contain minutes, so it cannot be converted to a minute-based output.";
		}

		if (outputPattern.contains("s") && !inputPattern.contains("s")) {
			return "Invalid conversion. The input format does not contain seconds, so it cannot be converted to a second-based output.";
		}

		if (outputPattern.contains("a") && !inputPattern.contains("a")) {
			return "Invalid conversion. The input format does not contain an AM/PM marker, so it cannot be converted to an AM/PM-based output.";
		}

		return null;
	}
}