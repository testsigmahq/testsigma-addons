package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.html5.Location;

@Data
@Action(actionText = "Change the geolocation to longitude, latitude, altitude",
        applicationType = ApplicationType.ANDROID,
        description = "This action can mock any location for a given latitude, longitude and altitude.")
public class SetGeoLocation extends AndroidAction {

    private static final String SUCCESS_MESSAGE = "Successfully Mocked the GeoLocation";
    private static final String FAILURE_MESSAGE = "Failed Mocked the GeoLocation";
    @TestData(reference = "longitude")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "latitude")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "altitude")
    private com.testsigma.sdk.TestData testData3;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        try{
            com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
            logger.info("Initiating execution");
            logger.debug("longitude: " + this.testData1.getValue() + ", latitude: " + this.testData2.getValue() + ", altitude : " + this.testData3.getValue());
            AndroidDriver androidDriver = (AndroidDriver) this.driver;
            double Lo = Double.parseDouble(testData1.getValue().toString().trim());
            double La = Double.parseDouble(testData2.getValue().toString().trim());
            double Al = Double.parseDouble(testData3.getValue().toString().trim());
            Location location = new Location(La, Lo, Al);
            androidDriver.setLocation(location);
            setSuccessMessage(String.format(SUCCESS_MESSAGE));
            return result;
        }catch(Exception e){
            setErrorMessage(String.format(FAILURE_MESSAGE));
            return Result.FAILED;
        }
    }

}