package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.Result;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.ConverterFunctions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
@Action(
        actionText = "Switch Location to coordinates latitude: lat-val , longitude: long-val , accuracy: acc-val",
        description = "Overrides geolocation using Chrome DevTools",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false
)
public class MockGeoLocationAction extends WebAction {

  @TestData(reference = "lat-val")
  private com.testsigma.sdk.TestData latVal;

  @TestData(reference = "long-val")
  private com.testsigma.sdk.TestData longVal;

  @TestData(reference = "acc-val")
  private com.testsigma.sdk.TestData accVal;

  @Override
  public Result execute() throws NoSuchElementException {
    Result result = Result.SUCCESS;

    try {
      // Input Validation
      double latitude, longitude, accuracy;
      try {
        latitude = Double.parseDouble(latVal.getValue().toString());
        logger.info("Parsed latitude: " + latitude);
      } catch (NumberFormatException e) {
        setErrorMessage("Invalid latitude format. Must be a number. Value provided: " + latVal.getValue());
        return Result.FAILED;
      }

      try {
        longitude = Double.parseDouble(longVal.getValue().toString());
        logger.info("Parsed longitude: " + longitude);
      } catch (NumberFormatException e) {
        setErrorMessage("Invalid longitude format. Must be a number. Value provided: " + longVal.getValue());
        return Result.FAILED;
      }

      try {
        accuracy = Double.parseDouble(accVal.getValue().toString());
        if (accuracy < 0) {
          setErrorMessage("Invalid accuracy value. Must be a non-negative number. Value provided: " + accuracy);
          return Result.FAILED;
        }
        logger.info("Parsed accuracy: " + accuracy);
      } catch (NumberFormatException e) {
        setErrorMessage("Invalid accuracy format. Must be a number. Value provided: " + accVal.getValue());
        return Result.FAILED;
      }

      Map<String, Object> coordinates = new HashMap<>();
      coordinates.put("latitude", latitude);
      coordinates.put("longitude", longitude);
      coordinates.put("accuracy", accuracy);

      try {
        // Enhance the driver to support DevTools
        logger.info("Augmenting driver to support DevTools...");
        WebDriver driver1 = new Augmenter().augment(this.driver);

        // Initialize DevTools and create a session
        logger.info("Initializing DevTools...");
        DevTools devTool = ((HasDevTools) driver1).getDevTools();
        devTool.createSessionIfThereIsNotOne();
        logger.info("DevTools session successfully created.");

        devTool.send(new Command<>("Emulation.setGeolocationOverride", coordinates, ConverterFunctions.empty()));
        logger.info("Geolocation override applied successfully using DevTools.");
      } catch (Exception e) {
        logger.info("DevTools usage failed (" + e.getClass().getSimpleName() + "), attempting fallback...");

        if (this.driver instanceof org.openqa.selenium.chromium.HasCdp) {
          ((org.openqa.selenium.chromium.HasCdp) this.driver).executeCdpCommand("Emulation.setGeolocationOverride",
                  coordinates);
          logger.info("Fallback to HasCdp (Chrome/Edge) execution successful.");
        } else if (this.driver instanceof ChromiumDriver) {
          ((ChromiumDriver) this.driver).executeCdpCommand("Emulation.setGeolocationOverride", coordinates);
          logger.info("Fallback to ChromiumDriver (Chrome/Edge) execution successful.");
        } else if (this.driver instanceof RemoteWebDriver) {
          // Reflection hack for RemoteWebDriver to bypass HasDevTools/Augmenter
          Method execute = RemoteWebDriver.class.getDeclaredMethod("execute", String.class, Map.class);
          execute.setAccessible(true);
          Map<String, Object> params = new HashMap<>();
          params.put("cmd", "Emulation.setGeolocationOverride");
          params.put("params", coordinates);
          execute.invoke(this.driver, "executeCdpCommand", params);
          logger.info("Fallback to RemoteWebDriver execution successful.");
        } else {
          throw e; // Rethrow if no fallback is possible
        }
      }

    } catch (Exception e) {
      logger.warn("Failed to override geolocation. Error: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to override geolocation. Error: " + ExceptionUtils.getMessage(e));
      result = Result.FAILED;
    }

    return result;
  }
}