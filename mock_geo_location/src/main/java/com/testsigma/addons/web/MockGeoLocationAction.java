package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.Result;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v124.emulation.Emulation;
import org.openqa.selenium.remote.Augmenter;

import java.util.Optional;

@Data
@Action(
        actionText = "Switch Location to coordinates latitude: lat-val , longitude: long-val , accuracy: acc-val",
        description = "Overrides geolocation using Chrome DevTools",
        applicationType = ApplicationType.WEB,
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
      } catch (NumberFormatException e) {
        setErrorMessage("Invalid latitude format. Must be a number. Value provided: " + latVal.getValue());
        return Result.FAILED;
      }

      try {
        longitude = Double.parseDouble(longVal.getValue().toString());
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
      } catch (NumberFormatException e) {
        setErrorMessage("Invalid accuracy format. Must be a number. Value provided: " + accVal.getValue());
        return Result.FAILED;
      }

      // Enhance the driver to support DevTools
      logger.info("Augmenting driver to support DevTools...");
      driver = new Augmenter().augment(driver);

      // Initialize DevTools and create a session
      logger.info("Initializing DevTools...");
      DevTools devTool = ((HasDevTools) driver).getDevTools();
      devTool.createSessionIfThereIsNotOne();
      logger.info("DevTools session successfully created.");

      devTool.send(Emulation.setGeolocationOverride(Optional.of(latitude), Optional.of(longitude), Optional.of(accuracy)));
      logger.info("Geolocation override applied successfully.");
    } catch (Exception e) {
      logger.warn("Failed to override geolocation. Error: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to override geolocation. Error: " + ExceptionUtils.getStackTrace(e));
      result = Result.FAILED;
    }

    return result;
  }
}