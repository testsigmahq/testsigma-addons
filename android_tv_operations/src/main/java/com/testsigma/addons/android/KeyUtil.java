package com.testsigma.addons.android;

import io.appium.java_client.android.nativekey.AndroidKey;

public class KeyUtil {
    
    public static AndroidKey getKey(String value) {
        AndroidKey androidKey;
        switch (value) {
            case "Up":
                androidKey = AndroidKey.DPAD_UP;
                break;
            case "Down":
                androidKey = AndroidKey.DPAD_DOWN;
                break;
            case "Left":
                androidKey = AndroidKey.DPAD_LEFT;
                break;
            case "Right":
                androidKey = AndroidKey.DPAD_RIGHT;
                break;
            case "Guide":
                androidKey = AndroidKey.GUIDE;
                break;
            case "Tv-contents-menu":
                androidKey = AndroidKey.TV_CONTENTS_MENU;
                break;
            case "Menu":
                androidKey = AndroidKey.MENU;
                break;
            case "Info":
                androidKey = AndroidKey.INFO;
                break;
            case "Ch+":
                androidKey = AndroidKey.CHANNEL_UP;
                break;
            case "Ch-":
                androidKey = AndroidKey.CHANNEL_DOWN;
                break;
            case "Back":
                androidKey = AndroidKey.BACK;
                break;
            case "Ok":
                androidKey = AndroidKey.DPAD_CENTER;
                break;
            case "NUM_0":
                androidKey = AndroidKey.DIGIT_0;
                break;
            case "NUM_1":
                androidKey = AndroidKey.DIGIT_1;
                break;
            case "NUM_2":
                androidKey = AndroidKey.DIGIT_2;
                break;
            case "NUM_3":
                androidKey = AndroidKey.DIGIT_3;
                break;
            case "NUM_4":
                androidKey = AndroidKey.DIGIT_4;
                break;
            case "NUM_5":
                androidKey = AndroidKey.DIGIT_5;
                break;
            case "NUM_6":
                androidKey = AndroidKey.DIGIT_6;
                break;
            case "NUM_7":
                androidKey = AndroidKey.DIGIT_7;
                break;
            case "NUM_8":
                androidKey = AndroidKey.DIGIT_8;
                break;
            case "NUM_9":
                androidKey = AndroidKey.DIGIT_9;
                break;
            case "Volume-Up":
                androidKey = AndroidKey.VOLUME_UP;
                break;
            case "Volume-Down":
                androidKey = AndroidKey.VOLUME_DOWN;
                break;
            case "PlayPause":
                androidKey = AndroidKey.MEDIA_PLAY_PAUSE;
                break;
            case "Rewind":
                androidKey = AndroidKey.MEDIA_REWIND;
                break;
            case "Forward":
                androidKey = AndroidKey.MEDIA_FAST_FORWARD;
                break;
            case "Home":
                androidKey = AndroidKey.HOME;
                break;
            case "Power":
                androidKey = AndroidKey.POWER;
                break;
            case "App-switch":
                androidKey = AndroidKey.APP_SWITCH;
                break;
            case "Soft-left":
                androidKey = AndroidKey.SOFT_LEFT;
                break;
            case "Soft-right":
                androidKey = AndroidKey.SOFT_RIGHT;
                break;
            case "Navigate-previous":
                androidKey = AndroidKey.NAVIGATE_PREVIOUS;
                break;
            case "Navigate-next":
                androidKey = AndroidKey.NAVIGATE_NEXT;
                break;
            case "Navigate-in":
                androidKey = AndroidKey.NAVIGATE_IN;
                break;
            case "Navigate-out":
                androidKey = AndroidKey.NAVIGATE_OUT;
                break;
            case "Stem-1-Netflix":
                androidKey = AndroidKey.STEM_1;
                break;
            case "Stem-2":
                androidKey = AndroidKey.STEM_2;
                break;
            case "Stem-3":
                androidKey = AndroidKey.STEM_3;
                break;

            default:
                throw new IllegalArgumentException("Invalid key value");
        }

        return androidKey;
    }
}
