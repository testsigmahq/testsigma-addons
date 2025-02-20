package com.testsigma.addons.rest_api;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
@Action(actionText = "Pick the current date dateformat by location timezone and store into a variable testdata",
        description = "pick the current date by timezone and store into a variable",
        applicationType = ApplicationType.REST_API,
        useCustomScreenshot = false)
public class Pickthedate extends RestApiAction {

  @TestData(reference = "dateformat")
  private com.testsigma.sdk.TestData testData1;
  @TestData(reference = "timezone")
  private com.testsigma.sdk.TestData testData2;
  @TestData(reference = "testdata" , isRuntimeVariable = true)
  private com.testsigma.sdk.TestData testData3;
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    
    try {
		ZoneId zoneId = ZoneId.of(testData2.getValue().toString());
		LocalDateTime currentDateTimeInZone = LocalDateTime.now(zoneId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(testData1.getValue().toString());
		String formattedDateTimeInZone = currentDateTimeInZone.format(formatter);
		
		runTimeData.setValue(formattedDateTimeInZone);
        runTimeData.setKey(testData3.getValue().toString());
        
        setSuccessMessage("Current date by timezone is :" +formattedDateTimeInZone+"store into a variable:"+testData3.getValue().toString());
		
	}catch (Exception e) {
		String errorMessage = ExceptionUtils.getStackTrace(e);
		result = com.testsigma.sdk.Result.FAILED;
		setErrorMessage(errorMessage);
		logger.warn(errorMessage);	
	} 
    return result;
  }
}