package com.testsigma.addons.mobile_web.util;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.Response;

import java.util.LinkedHashSet;
import java.util.Set;

public class ContextUtils {
    public static Set<String> getContextHandles(AppiumDriver driver) {
        Response response = driver.execute(DriverCommand.GET_CONTEXT_HANDLES, ImmutableMap.of());
        Object value = response.getValue();
        try {
            //noinspection unchecked
            java.util.List<String> returnedValues = (java.util.List<String>) value;
            return new LinkedHashSet<>(returnedValues);
        } catch (ClassCastException ex) {
            throw new WebDriverException(
                    "Returned value cannot be converted to List<String>: " + value, ex);
        }
    }

    public static String getCurrentContext(AppiumDriver driver) {
        Response response = driver.execute(DriverCommand.GET_CURRENT_CONTEXT_HANDLE, ImmutableMap.of());
        Object value = response.getValue();
        try {
            //noinspection unchecked
            return (String) value;
        } catch (ClassCastException ex) {
            throw new WebDriverException(
                    "Returned value cannot be converted to List<String>: " + value, ex);
        }
    }
}
