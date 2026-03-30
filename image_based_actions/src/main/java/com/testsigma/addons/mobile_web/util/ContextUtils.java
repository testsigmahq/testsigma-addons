package com.testsigma.addons.mobile_web.util;

import io.appium.java_client.AppiumDriver;
import java.util.Set;

public class ContextUtils {
    public static Set<String> getContextHandles(AppiumDriver driver) {
        return ((io.appium.java_client.remote.SupportsContextSwitching) driver).getContextHandles();
    }

    public static String getCurrentContext(AppiumDriver driver) {
        return ((io.appium.java_client.remote.SupportsContextSwitching) driver).getContext();
    }
}
