package com.testsigma.addons.android;

import io.appium.java_client.CommandExecutionHelper;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebDriverException;

import java.util.HashMap;
import java.util.Map;

public final class PressKeycodeHelper {

    private static final String EXT_NAME = "mobile: pressKey";
    private static final String FALLBACK_COMMAND = "pressKeycode";
    private static final int TV_REMOTE_DELAY_MS = 2000;

    private PressKeycodeHelper() {
    }

    public static void pressKeyTvRemote(AndroidDriver driver, int keycode) {
        try {
            Thread.sleep(TV_REMOTE_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("TV remote key delay interrupted", e);
        }
        String keyName = KeyUtil.getKeyName(keycode);
        System.out.println("Sending " + keyName + " from TV REMOTE");
        pressKeycode(driver, keycode);
    }

    public static void pressKeycode(AndroidDriver driver, int keycode,
                                    Integer metastate, Integer flags) {
        Map<String, Object> args = new HashMap<>();
        args.put("keycode", keycode);
        if (metastate != null) {
            args.put("metastate", metastate);
        }
        if (flags != null) {
            args.put("flags", flags);
        }

        try {
            CommandExecutionHelper.executeScript(driver, EXT_NAME, args);
        } catch (WebDriverException e) {
            // Fallback for servers that do not support the "mobile: pressKey" extension
            driver.execute(FALLBACK_COMMAND, args);
        }
    }

    public static void pressKeycode(AndroidDriver driver, int keycode) {
        pressKeycode(driver, keycode, null, null);
    }
}
