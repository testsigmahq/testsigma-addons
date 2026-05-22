package com.testsigma.addons.network_throttling.web;

import com.testsigma.sdk.WebAction;
import com.google.common.collect.ImmutableMap;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@Action(actionText = "Simulate network to no network mode",
        description = "Simulates complete network offline in the browser",
        applicationType = ApplicationType.WEB)
public class NetworkOffline extends WebAction {

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

    try {
      CommandExecutor exe = ((RemoteWebDriver) driver).getCommandExecutor();

      Map map = new HashMap();
      map.put("offline", true);
      map.put("latency", 0);
      map.put("download_throughput", 0);
      map.put("upload_throughput", 0);

      Response response = exe.execute(
              new Command(((RemoteWebDriver) driver).getSessionId(), "setNetworkConditions",
                      ImmutableMap.of("network_conditions", ImmutableMap.copyOf(map)))
      );
      setSuccessMessage("Network is now set to offline. Browser has no network access.");
    } catch (IOException e) {
      setErrorMessage("Exception in setting network to offline.Error : " +  ExceptionUtils.getMessage(e));
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
    }

    return result;
  }
}
