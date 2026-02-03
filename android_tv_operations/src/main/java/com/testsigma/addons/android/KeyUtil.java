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

    /**
     * Returns Android KeyEvent keycode (int) for the given key name.
     * Use with pressKeyCode(keycode) for reliable key injection on Android TV
     * (works for Down, Menu and other keys where pressKey(KeyEvent) may fail).
     * Keycodes match android.view.KeyEvent constants.
     */
    public static int getKeyCode(String value) {
        switch (value) {
            case "Up": return 19;           // KEYCODE_DPAD_UP
            case "Down": return 20;          // KEYCODE_DPAD_DOWN
            case "Left": return 21;          // KEYCODE_DPAD_LEFT
            case "Right": return 22;         // KEYCODE_DPAD_RIGHT
            case "Ok": return 23;            // KEYCODE_DPAD_CENTER
            case "Guide": return 172;        // KEYCODE_TV_GUIDE
            case "Tv-contents-menu": return 256; // KEYCODE_TV_CONTENTS_MENU
            case "Menu": return 82;          // KEYCODE_MENU
            case "Info": return 165;         // KEYCODE_INFO
            case "Ch+": return 166;          // KEYCODE_CHANNEL_UP
            case "Ch-": return 167;          // KEYCODE_CHANNEL_DOWN
            case "Back": return 4;           // KEYCODE_BACK
            case "NUM_0": return 7;          // KEYCODE_0
            case "NUM_1": return 8;          // KEYCODE_1
            case "NUM_2": return 9;          // KEYCODE_2
            case "NUM_3": return 10;         // KEYCODE_3
            case "NUM_4": return 11;         // KEYCODE_4
            case "NUM_5": return 12;         // KEYCODE_5
            case "NUM_6": return 13;        // KEYCODE_6
            case "NUM_7": return 14;        // KEYCODE_7
            case "NUM_8": return 15;        // KEYCODE_8
            case "NUM_9": return 16;        // KEYCODE_9
            case "Volume-Up": return 24;     // KEYCODE_VOLUME_UP
            case "Volume-Down": return 25;  // KEYCODE_VOLUME_DOWN
            case "PlayPause": return 85;    // KEYCODE_MEDIA_PLAY_PAUSE
            case "Rewind": return 89;       // KEYCODE_MEDIA_REWIND
            case "Forward": return 90;      // KEYCODE_MEDIA_FAST_FORWARD
            case "Home": return 3;          // KEYCODE_HOME
            case "Power": return 26;        // KEYCODE_POWER
            case "App-switch": return 187;  // KEYCODE_APP_SWITCH
            case "Soft-left": return 1;     // KEYCODE_SOFT_LEFT
            case "Soft-right": return 2;    // KEYCODE_SOFT_RIGHT
            case "Navigate-previous": return 122; // KEYCODE_NAVIGATE_PREVIOUS
            case "Navigate-next": return 123;     // KEYCODE_NAVIGATE_NEXT
            case "Navigate-in": return 124;      // KEYCODE_NAVIGATE_IN
            case "Navigate-out": return 125;     // KEYCODE_NAVIGATE_OUT
            case "Stem-1-Netflix": return 301;   // KEYCODE_STEM_1
            case "Stem-2": return 302;           // KEYCODE_STEM_2
            case "Stem-3": return 303;           // KEYCODE_STEM_3
            default:
                throw new IllegalArgumentException("Invalid key value");
        }
    }

    public static String getKeyName(int keycode) {
        switch (keycode) {
            case 19:  return "Up";
            case 20:  return "Down";
            case 21:  return "Left";
            case 22:  return "Right";
            case 23:  return "Ok";
            case 172: return "Guide";
            case 256: return "Tv-contents-menu";
            case 82:  return "Menu";
            case 165: return "Info";
            case 166: return "Ch+";
            case 167: return "Ch-";
            case 4:   return "Back";
            case 7:   return "NUM_0";
            case 8:   return "NUM_1";
            case 9:   return "NUM_2";
            case 10:  return "NUM_3";
            case 11:  return "NUM_4";
            case 12:  return "NUM_5";
            case 13:  return "NUM_6";
            case 14:  return "NUM_7";
            case 15:  return "NUM_8";
            case 16:  return "NUM_9";
            case 24:  return "Volume-Up";
            case 25:  return "Volume-Down";
            case 85:  return "PlayPause";
            case 89:  return "Rewind";
            case 90:  return "Forward";
            case 3:   return "Home";
            case 26:  return "Power";
            case 187: return "App-switch";
            case 1:   return "Soft-left";
            case 2:   return "Soft-right";
            case 122: return "Navigate-previous";
            case 123: return "Navigate-next";
            case 124: return "Navigate-in";
            case 125: return "Navigate-out";
            case 301: return "Stem-1-Netflix";
            case 302: return "Stem-2";
            case 303: return "Stem-3";
            default:  return "keycode_" + keycode;
        }
    }
}
