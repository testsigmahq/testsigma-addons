package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.KeyEventMetaModifier;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Action(actionText = "Enter data inside an element using keypad", applicationType = ApplicationType.ANDROID)
public class AndroidKeyEvent extends AndroidAction {

  @TestData(reference = "data")
  private com.testsigma.sdk.TestData testData;
  @Element(reference = "element")
  private com.testsigma.sdk.Element element;

  @Override
  protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");
    logger.debug("Element locator: "+ this.element.getValue() +" by:"+ this.element.getBy() + ", test-data: "+ this.testData.getValue());
    AndroidDriver d = (AndroidDriver)this.driver;
    WebElement webElement = element.getElement();
    
   if(webElement.isDisplayed() || webElement.isEnabled()) {
	   
	   logger.info("Element located going for next steps");
	   
   }else {
	   result=com.testsigma.sdk.Result.FAILED;
	   logger.debug("NO SUCH ELEMENT FOUND");
   }
   
   try{
	   
 	  char[] arr = testData.getValue().toString().toCharArray();
 	  
    
     for(char c:arr){
     	
        if(Character.isLowerCase(c)){
     	   d.pressKey(new KeyEvent(Enum.valueOf(AndroidKey.class, String.valueOf(c).toUpperCase())));
          }
        else if(Character.isUpperCase(c)){
     	   d.pressKey(new KeyEvent(Enum.valueOf(AndroidKey.class, String.valueOf(c))).withMetaModifier(KeyEventMetaModifier.SHIFT_ON));
     	 
        }
        else if(String.valueOf(c).equals("@")){
     	   d.pressKey(new KeyEvent(AndroidKey.AT)); 
        }
        else if(String.valueOf(c).equals("=")){
     	   d.pressKey(new KeyEvent(AndroidKey.EQUALS));
        }
        
        else if(String.valueOf(c).equals("#")){
     	   d.pressKey(new KeyEvent(AndroidKey.POUND)); 
        }
        
        else if(String.valueOf(c).equals("/")){
     	   d.pressKey(new KeyEvent(AndroidKey.SLASH)); 
        }
        else if(String.valueOf(c).equals("*")){
     	   d.pressKey(new KeyEvent(AndroidKey.STAR)); 
        }
        else if(String.valueOf(c).equals("+")){
     	   d.pressKey(new KeyEvent(AndroidKey.PLUS)); 
        }
        else if(String.valueOf(c).equals("-")){
     	   d.pressKey(new KeyEvent(AndroidKey.MINUS)); 
        }
        else if(String.valueOf(c).equals("[")){
     	   d.pressKey(new KeyEvent(AndroidKey.LEFT_BRACKET)); 
        }
        else if(String.valueOf(c).equals("]")){
     	   d.pressKey(new KeyEvent(AndroidKey.RIGHT_BRACKET)); 
        }
        else if(String.valueOf(c).equals(".")){
     	    d.pressKey(new KeyEvent(AndroidKey.PERIOD)); 
        }
        else if(String.valueOf(c).equals(";")){
     	   d.pressKey(new KeyEvent(AndroidKey.SEMICOLON)); 
        }
        
        else if(String.valueOf(c).equals(" ")) {
         	   d.pressKey(new KeyEvent(AndroidKey.SPACE)); 
            }

        else if(String.valueOf(c).equals("1")) {
         	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_1)); 
         	 	   
            }

        else if(String.valueOf(c).equals("0")) {
         	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_0)); 
            }

        else if(String.valueOf(c).equals("2")) {
         	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_2)); 
            }

        else if(String.valueOf(c).equals("3")) {
         	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_3)); 
            }

        else if(String.valueOf(c).equals("4")) {
         	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_4)); 
            }

        else if(String.valueOf(c).equals("5")) {
         	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_5)); 
            }

        else if(String.valueOf(c).equals("6")) {
         	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_6)); 
            }
        else if(String.valueOf(c).equals("7")) {
     	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_7)); 
        }
        else if(String.valueOf(c).equals("8")) {
     	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_8)); 
        }
        else if(String.valueOf(c).equals("9")) {
     	   d.pressKey(new KeyEvent(AndroidKey.DIGIT_9)); 
        } 
        else {
        	
        	 logger.debug("Operation failed with data provided");
        }
   }
     
   }catch(Exception e) {
	   
	   e.printStackTrace();
	   logger.debug(e.getMessage()+e.getCause());
	   result=com.testsigma.sdk.Result.FAILED;
	   setErrorMessage("Operation failed. Please check the log for more details");
	     
   }
   return result;
  }
  
}
   