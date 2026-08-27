package com.testsigma.addons.web;

import com.testsigma.addons.util.EdgeSitePermissions;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WindowType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Action(actionText = "Set edge permission to URL: Target-URL , Permission Name: Permission-Name, Permission Type: Permission-Value",
        description = "Set edge site settings permissions to URL.",
        applicationType = ApplicationType.WEB)
public class EdgePermssion extends WebAction {

	  @TestData(reference = "Target-URL")
	  private com.testsigma.sdk.TestData targetUrl;

	  @TestData(reference = "Permission-Name")
	  private com.testsigma.sdk.TestData permissionName;

	  @TestData(reference = "Permission-Value")
	  private com.testsigma.sdk.TestData permissionValue;

	  /** Time allowed for the settings page to render before it is read. */
	  private static final long SETTLE_MS = 5000L;

	  @Override
	  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
	    String name = permissionName.getValue().toString().trim();
	    String value = permissionValue.getValue().toString().trim();

	    String settingsUrl;
	    try {
	      settingsUrl = EdgeSitePermissions.settingsUrl(targetUrl.getValue().toString());
	    } catch (IllegalArgumentException e) {
	      setErrorMessage(e.getMessage());
	      return com.testsigma.sdk.Result.FAILED;
	    }

	    String original = driver.getWindowHandle();
	    List<String> existing = new ArrayList<>(driver.getWindowHandles());
	    Optional<String> failure;
	    try {
	      driver.switchTo().newWindow(WindowType.TAB);
	      String tab = driver.getWindowHandles().stream()
	          .filter(handle -> !existing.contains(handle))
	          .findFirst()
	          .orElseThrow(() -> new NoSuchElementException("New tab not found!"));
	      driver.switchTo().window(tab);

	      logger.info("opening " + settingsUrl);
	      driver.get(settingsUrl);
	      Thread.sleep(SETTLE_MS);

	      failure = EdgeSitePermissions.set(driver, logger, name, value);
	    } catch (Exception e) {
	      logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
	      logger.info("Error occurred while changing site permissions: "
	          + ExceptionUtils.getMessage(e));
	      setErrorMessage("Unable to change site permissions, " + ExceptionUtils.getMessage(e));
	      return com.testsigma.sdk.Result.FAILED;
	    } finally {
	      try {
	        driver.switchTo().window(original);
	      } catch (Exception e) {
	        logger.info("could not return to the original window: "
	            + ExceptionUtils.getMessage(e));
	      }
	    }

	    if (failure.isPresent()) {
	      setErrorMessage(failure.get());
	      return com.testsigma.sdk.Result.FAILED;
	    }
	    setSuccessMessage("Site permissions changed successfully");
	    return com.testsigma.sdk.Result.SUCCESS;
	  }
}
