package com.testsigma.addons.network_throttling.web;

import com.testsigma.sdk.WebAction;
import com.google.common.collect.ImmutableMap;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@Action(actionText = "Simulate network to upload_speed upload speed(kbps) download_speed download speed(kbps) latency_time latency(ms)",
        description = "Changes network based on input speed and latency",
        applicationType = ApplicationType.WEB)
public class NetworkThrottling extends WebAction {

  @TestData(reference = "upload_speed")
  private com.testsigma.sdk.TestData testData1;
  @TestData(reference = "download_speed")
  private com.testsigma.sdk.TestData testData2;
  @TestData(reference = "latency_time")
  private com.testsigma.sdk.TestData testData3;
  

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
  
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    
    if(Integer.parseInt(testData1.getValue().toString()) >0 && Integer.parseInt(testData2.getValue().toString()) >0) {
		CommandExecutor exe= ((RemoteWebDriver) driver).getCommandExecutor();
		
		Map map=new HashMap();
		map.put("offline", false);
		map.put("latency", Integer.parseInt(testData3.getValue().toString()));
		map.put("download_throughput", Integer.parseInt(testData2.getValue().toString()));
		map.put("upload_throughput", Integer.parseInt(testData1.getValue().toString()));
		
		try {
			Response response = exe.execute(
			        new Command(((RemoteWebDriver)driver).getSessionId(),"setNetworkConditions", ImmutableMap.of("network_conditions", ImmutableMap.copyOf(map)))
			  );			
			setSuccessMessage("Simulated Network as per given data. Upload speed is now "+testData1.getValue().toString()+" Download Speed is now "+testData2.getValue().toString()+" and Latency time now is "+testData3.getValue().toString());
		} catch (IOException e) {
			setErrorMessage("Exception in Setting Network Condtion. Check log for details");
			result = com.testsigma.sdk.Result.FAILED;
			logger.warn(e.getMessage().toString());
			e.printStackTrace();
		}	
	} 
    
    else {
    	logger.warn(String.format("Check the value of the speed entered....value cant be Zero"));
    	setErrorMessage("Cant simulate network. Please verify if the speeds and latency are more than 0");
    	result = com.testsigma.sdk.Result.FAILED;
    }
    return result;
  }
}