package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@Data
@Action(actionText = "Click on element using its coordinates",
        description = "Click on the web elemenet using it location coordinates in web application",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class ClickOnElementUsingCoordinates extends WebAction {

  @Element(reference = "element")
  private com.testsigma.sdk.Element element;
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    logger.debug("Element: "+ this.element.getValue() +" by:"+ this.element.getBy());
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    
    try {
    	WebElement RFelement = driver.findElement(element.getBy());
        Actions actions=new Actions(driver);
    	
        
    	int x=RFelement.getRect().getX();
    	int y=RFelement.getRect().getY();
    	int h=RFelement.getRect().getHeight()/2;
    	int w=RFelement.getRect().getWidth()/2;
    	
    	actions.moveByOffset(x+w, y+h).click().perform();
    	
    	setSuccessMessage("Successfully performed click action on the given Element");
    } catch (AssertionError error) {
    	 result = com.testsigma.sdk.Result.FAILED;
         logger.warn("Unable to perform click action on the given Element. Please check the Locator value");
         setErrorMessage("Unable to perform click action on the given Element. Please check the Locator value");
    }
    return result;
  }
}