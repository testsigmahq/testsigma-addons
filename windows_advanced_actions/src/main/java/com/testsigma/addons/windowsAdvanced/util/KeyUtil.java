package com.testsigma.addons.windowsAdvanced.util;

import com.testsigma.sdk.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.event.KeyEvent;

public class KeyUtil {
    private Logger logger;

    public KeyUtil(Logger logger) {
        this.logger = logger;
    }


    public int getKeyCode(String keyName) throws Exception {
        if (keyName.length() == 1) {
            char character = keyName.charAt(0);
            if (Character.isUpperCase(character)) {
                return character; // KeyEvent.VK_A through KeyEvent.VK_Z are the same as ASCII 'A' through 'Z'
            } else if (Character.isLowerCase(character)) {
                return Character.toUpperCase(character); // Convert lowercase to uppercase keycode
            } else if (Character.isDigit(character)) {
                return character; // KeyEvent.VK_0 through KeyEvent.VK_9 are the same as ASCII '0' through '9'
            } else {
                logger.info("unsupported key: " + keyName);
                throw new Exception("unsupported key: " + keyName);
            }
        }

        switch (keyName) {
            case "Tab":
                return KeyEvent.VK_TAB;
            case "Enter":
                return KeyEvent.VK_ENTER;
            case "Caps-Lock":
                return KeyEvent.VK_CAPS_LOCK;
            case "Shift":
                return KeyEvent.VK_SHIFT;
            case "Control":
                return KeyEvent.VK_CONTROL;
            case "AltKey":
                return KeyEvent.VK_ALT;
            case "Backspace":
                return KeyEvent.VK_BACK_SPACE;
            case "Delete":
                return KeyEvent.VK_DELETE;
            case "Insert":
                return KeyEvent.VK_INSERT;
            case "Home":
                return KeyEvent.VK_HOME;
            case "End":
                return KeyEvent.VK_END;
            case "Page-Up":
                return KeyEvent.VK_PAGE_UP;
            case "Page-Down":
                return KeyEvent.VK_PAGE_DOWN;
            case "F1":
                return KeyEvent.VK_F1;
            case "F2":
                return KeyEvent.VK_F2;
            case "F3":
                return KeyEvent.VK_F3;
            case "F4":
                return KeyEvent.VK_F4;
            case "F5":
                return KeyEvent.VK_F5;
            case "F6":
                return KeyEvent.VK_F6;
            case "F7":
                return KeyEvent.VK_F7;
            case "F8":
                return KeyEvent.VK_F8;
            case "F9":
                return KeyEvent.VK_F9;
            case "F10":
                return KeyEvent.VK_F10;
            case "F11":
                return KeyEvent.VK_F11;
            case "F12":
                return KeyEvent.VK_F12;
            case "Print-Screen":
                return KeyEvent.VK_PRINTSCREEN;
            case "Scroll-Lock":
                return KeyEvent.VK_SCROLL_LOCK;
            case "Pause-Break":
                return KeyEvent.VK_PAUSE;
            case "Space":
                return KeyEvent.VK_SPACE;
            case "Comma":
                return KeyEvent.VK_COMMA;
            case "Period":
                return KeyEvent.VK_PERIOD;
            case "Minus":
                return KeyEvent.VK_MINUS;
            case "Equals":
                return KeyEvent.VK_EQUALS;
            case "Colon":
                return KeyEvent.VK_COLON;
            case "Semi-Colon":
                return KeyEvent.VK_SEMICOLON;
            case "Slash":
                return KeyEvent.VK_SLASH;
            case "Back-Slash":
                return KeyEvent.VK_BACK_SLASH;
            case "Escape":
                return KeyEvent.VK_ESCAPE;//  "Arrow-Left", "Arrow-Right", "Arrow-Up", "Arrow-Down"
            case "Arrow-Left":
                return KeyEvent.VK_LEFT;
            case "Arrow-Right":
                return KeyEvent.VK_RIGHT;
            case "Arrow-Up":
                return KeyEvent.VK_UP;
            case "Arrow-Down":
                return KeyEvent.VK_DOWN;
            default:
                logger.info("unsupported key: " + keyName);
                throw new Exception("unsupported key: " + keyName);
        }
    }

    public void releaseKey(Robot robot, int keyCode) throws Exception {
        robot.keyRelease(keyCode);
    }

    public void pressKey(Robot robot, int keyCode) throws Exception {
        robot.keyPress(keyCode);
    }



    public void pressAndReleaseInCombination(Robot robot, String[] combinationKeys) throws Exception {
        for (String key : combinationKeys) {
            try {
                pressKey(robot, getKeyCode(key));
                robot.delay(2);
            } catch (Exception e) {
                logger.info("Error while pressing key: " + key + " in combination: " + ExceptionUtils.getStackTrace(e));
                throw e;
            }
        }
        for (int i= combinationKeys.length - 1; i >= 0; i--) {
            try {
                releaseKey(robot, getKeyCode(combinationKeys[i]));
                robot.delay(300);
            } catch (Exception e) {
                logger.info("Error while releasing key: " + combinationKeys[i] + " in combination: " + ExceptionUtils.getStackTrace(e));
                throw e;
            }
        }
    }
}