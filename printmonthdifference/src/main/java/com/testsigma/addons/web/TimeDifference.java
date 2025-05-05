package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
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
@Action(actionText = "Calculate the time difference between starttime start_time and endtime end_time with format time_format and store into runtime variable(hh:mm:ss) testdata",
description = "Calculate the time difference and store into a varaible",
applicationType = ApplicationType.WEB,
useCustomScreenshot = false)
public class TimeDifference extends WebAction {

	@TestData(reference = "start_time")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "end_time")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "time_format")
	private com.testsigma.sdk.TestData testData3;
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
		
		String starttime = testData1.getValue().toString();
        String endtime = testData2.getValue().toString();

        SimpleDateFormat format = new SimpleDateFormat(testData3.getValue().toString());

		try {
            Date datestart = format.parse(starttime);
            Date dateend = format.parse(endtime);

            long difference = dateend.getTime() - datestart.getTime();

            long hours = difference / (60 * 60 * 1000) % 24;
            long minutes = difference / (60 * 1000) % 60;
            long seconds = difference / 1000 % 60;
            
            String timeDifference = hours + ":" + minutes + ":" + seconds;

            runTimeData.setValue(timeDifference);
            runTimeData.setKey(testData4.getValue().toString());
            setSuccessMessage("Storing the time difference : " +timeDifference +" : into to the varaible :"+testData4.getValue().toString());
            logger.info("Storing the time difference : " +timeDifference +" : into to the varaible :"+testData4.getValue().toString());
        } catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		} 
		return result;
	}
}