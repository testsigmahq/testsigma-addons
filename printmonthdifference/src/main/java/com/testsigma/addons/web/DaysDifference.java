package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;

import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Data
@Action(actionText = "Calculate the days difference between startdate start_date and enddate end_date with format date_format and store into runtime variable(output_format) testdata",
description = "Calculate the days difference and store into a varaible",
applicationType = ApplicationType.WEB,
useCustomScreenshot = false)
public class DaysDifference extends WebAction {

	@TestData(reference = "start_date")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "end_date")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "date_format")
	private com.testsigma.sdk.TestData testData3;
	@TestData(reference = "output_format",allowedValues ={"Days","Weeks"})
	private com.testsigma.sdk.TestData format;

	@TestData(reference = "testdata" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData4;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		//Your Awesome code starts here
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(testData3.getValue().toString());

		try {
			LocalDate startDate = LocalDate.parse(testData1.getValue().toString(), formatter);
			LocalDate endDate = LocalDate.parse(testData2.getValue().toString(), formatter);

			//Period period = Period.between(startDate, endDate);
			long totalDays = ChronoUnit.DAYS.between(startDate, endDate);

			String outputformat = format.getValue().toString();

			switch (outputformat) {
			case "Days":
				runTimeData.setValue(Long.toString(totalDays));
				runTimeData.setKey(testData4.getValue().toString());
				setSuccessMessage("Storing the days difference : " +totalDays +" : into to the varaible :"+testData4.getValue().toString());
				logger.info("Storing the days difference : " +totalDays +" : into to the varaible :"+testData4.getValue().toString());
				break;
			case "Weeks":
				long weeks = totalDays / 7;
				runTimeData.setValue(Long.toString(weeks));
				runTimeData.setKey(testData4.getValue().toString());
				setSuccessMessage("Storing the Weeks difference : " +weeks +" : into to the varaible :"+testData4.getValue().toString());
				logger.info("Storing the Weeks difference : " +weeks +" : into to the varaible :"+testData4.getValue().toString());
				break;
			}
		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		} 
		return result;
	}
}