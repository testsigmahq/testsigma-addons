package com.testsigma.addons.mobile_gestures.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Do Shake Action(Only for emulators)", applicationType = ApplicationType.IOS)
public class ShakeGesture extends IOSAction {


    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        IOSDriver iosDriver = (IOSDriver) this.driver;
        iosDriver.shake();
        setSuccessMessage("Shake Gesture Successfully performed.");
        return result;
    }


}

