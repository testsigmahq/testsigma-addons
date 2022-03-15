package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if string1 matches with string2",
        description = "Verify if both the string matches",
        applicationType = ApplicationType.MOBILE_WEB)
public class StringCompareMw extends StringCompareWeb {

  @TestData(reference = "string1")
  private com.testsigma.sdk.TestData testData1;
  @TestData(reference = "string2")
  private com.testsigma.sdk.TestData testData2;
  
  
  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
	  super.setTestData1(this.testData1);
  	  super.setTestData2(this.testData2);
	  return super.execute();
  }
}