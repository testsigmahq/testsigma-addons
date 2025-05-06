package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Data
@Action(actionText = "Print the month difference between startdate start(dd/MM/yyyy) and enddate end(dd/MM/yyyy) and store into runtime variable testdata",
description = "print the month difference and store into a varaible",
applicationType = ApplicationType.ANDROID,
useCustomScreenshot = false)
public class MonthDifference extends AndroidAction {

	@TestData(reference = "start(dd/MM/yyyy)")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "end(dd/MM/yyyy)")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "testdata" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData3;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		//Your Awesome code starts here
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		try {
			String startDateString = testData1.getValue().toString();
			String endDateString = testData2.getValue().toString();

			LocalDate startDate = LocalDate.parse(startDateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			LocalDate endDate = LocalDate.parse(endDateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

			printMonthNames(startDate, endDate);

		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		} 
		return result;
	}

	private void printMonthNames(LocalDate startDate, LocalDate endDate) {
		int monthCount = 0;
		while (!startDate.isAfter(endDate)) {
			String monthnames = startDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
			sb.append(monthnames);
			monthCount++;

			if (!startDate.plus(1, ChronoUnit.MONTHS).isAfter(endDate)) {
				sb.append(",");
			}
			startDate = startDate.plus(1, ChronoUnit.MONTHS);
		}
		runTimeData.setValue(Integer.toString(monthCount));
        runTimeData.setKey(testData3.getValue().toString());
        
        setSuccessMessage("The difference in months is: " +monthCount +" "+ "(" +sb.toString()+ ")");
        logger.info("The difference in months is: " +monthCount +" "+ "(" +sb.toString()+ ")");
        logger.info("Storing the month difference : " +monthCount +" : into to the varaible :"+testData3.getValue().toString());
	}
}