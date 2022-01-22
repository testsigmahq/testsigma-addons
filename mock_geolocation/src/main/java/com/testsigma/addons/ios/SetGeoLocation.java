package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.html5.Location;

@Data
@Action(actionText = "Change the geolocation to longitude, latitude,altitude", applicationType = ApplicationType.IOS)
public class SetGeoLocation extends IOSAction {

    private static final String SUCCESS_MESSAGE = "Successfully Mocked the GeoLocation";
    @TestData(reference = "longitude")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "latitude")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "altitude")
    private com.testsigma.sdk.TestData testData3;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("longitude: " + this.testData1.getValue() + ", latitude: " + this.testData2.getValue() + ", altitude : " + this.testData3.getValue());
        IOSDriver iosDriver = (IOSDriver) this.driver;
        int Lo = Integer.valueOf(testData1.getValue().toString());
        int La = Integer.valueOf(testData2.getValue().toString());
        int Al = Integer.valueOf(testData3.getValue().toString());
        Location location = new Location(La, Lo, Al);
        iosDriver.setLocation(location);

        setSuccessMessage(String.format(SUCCESS_MESSAGE));

        return result;
    }
}