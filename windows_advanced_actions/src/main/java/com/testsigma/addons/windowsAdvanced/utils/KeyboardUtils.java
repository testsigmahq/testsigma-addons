package com.testsigma.addons.windowsAdvanced.utils;

import java.awt.event.KeyEvent;

/**
 * Utility class for keyboard operations and key code mappings
 */
public class KeyboardUtils {

    // Allowed values for modifier keys
    public static final String[] MODIFIER_KEYS = {"Alt", "Ctrl", "Enter", "Shift", "Tab", "WINDOW"};
    
    // Allowed values for alphanumeric keys
    public static final String[] ALPHANUMERIC_KEYS = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
        "U", "V", "W", "X", "Y", "Z",
        "Space", "Comma", "Period", "Semicolon", "Colon", "Exclamation", "Question",
        "At", "Hash", "Dollar", "Percent", "Caret", "Ampersand", "Asterisk",
        "Left_Parenthesis", "Right_Parenthesis", "Minus", "Plus", "Equals",
        "Left_Bracket", "Right_Bracket", "Backslash", "Forward_Slash", "Pipe",
        "Left_Brace", "Right_Brace", "Tilde", "Backtick", "Quote", "Double_Quote"
    };

    // Allowed values for specific keys
    public static final String[] SPECIFIC_KEYS = {
        "Space", "Backspace", "Delete", "Escape", "Home", "End", 
        "Page_Up", "Page_Down", "Insert", "F1", "F2", "F3", "F4", 
        "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"
    };

    /**
     * Maps the modifier key string to its corresponding KeyEvent constant
     * @param key The modifier key string
     * @return The corresponding KeyEvent constant
     * @throws IllegalArgumentException if the key is not supported
     */
    public static int getModifierKeyCode(String key) {
        switch (key.toLowerCase()) {
            case "alt":
                return KeyEvent.VK_ALT;
            case "ctrl":
                return KeyEvent.VK_CONTROL;
            case "enter":
                return KeyEvent.VK_ENTER;
            case "shift":
                return KeyEvent.VK_SHIFT;
            case "tab":
                return KeyEvent.VK_TAB;
            case "window":
                return KeyEvent.VK_META;
            default:
                throw new IllegalArgumentException("Unsupported modifier key: " + key);
        }
    }

    /**
     * Maps the alphanumeric key string to its corresponding KeyEvent constant
     * @param key The alphanumeric key string
     * @return The corresponding KeyEvent constant
     * @throws IllegalArgumentException if the key is not supported
     */
    public static int getAlphanumericKeyCode(String key) {
        // Handle digits
        if (key.length() == 1 && Character.isDigit(key.charAt(0))) {
            return KeyEvent.getExtendedKeyCodeForChar(key.charAt(0));
        }
        
        // Handle letters
        if (key.length() == 1 && Character.isLetter(key.charAt(0))) {
            return KeyEvent.getExtendedKeyCodeForChar(Character.toUpperCase(key.charAt(0)));
        }
        
        // Handle specific key mappings
        switch (key.toUpperCase()) {
            case "0":
                return KeyEvent.VK_0;
            case "1":
                return KeyEvent.VK_1;
            case "2":
                return KeyEvent.VK_2;
            case "3":
                return KeyEvent.VK_3;
            case "4":
                return KeyEvent.VK_4;
            case "5":
                return KeyEvent.VK_5;
            case "6":
                return KeyEvent.VK_6;
            case "7":
                return KeyEvent.VK_7;
            case "8":
                return KeyEvent.VK_8;
            case "9":
                return KeyEvent.VK_9;
            case "A":
                return KeyEvent.VK_A;
            case "B":
                return KeyEvent.VK_B;
            case "C":
                return KeyEvent.VK_C;
            case "D":
                return KeyEvent.VK_D;
            case "E":
                return KeyEvent.VK_E;
            case "F":
                return KeyEvent.VK_F;
            case "G":
                return KeyEvent.VK_G;
            case "H":
                return KeyEvent.VK_H;
            case "I":
                return KeyEvent.VK_I;
            case "J":
                return KeyEvent.VK_J;
            case "K":
                return KeyEvent.VK_K;
            case "L":
                return KeyEvent.VK_L;
            case "M":
                return KeyEvent.VK_M;
            case "N":
                return KeyEvent.VK_N;
            case "O":
                return KeyEvent.VK_O;
            case "P":
                return KeyEvent.VK_P;
            case "Q":
                return KeyEvent.VK_Q;
            case "R":
                return KeyEvent.VK_R;
            case "S":
                return KeyEvent.VK_S;
            case "T":
                return KeyEvent.VK_T;
            case "U":
                return KeyEvent.VK_U;
            case "V":
                return KeyEvent.VK_V;
            case "W":
                return KeyEvent.VK_W;
            case "X":
                return KeyEvent.VK_X;
            case "Y":
                return KeyEvent.VK_Y;
            case "Z":
                return KeyEvent.VK_Z;
            case "SPACE":
                return KeyEvent.VK_SPACE;
            case "COMMA":
                return KeyEvent.VK_COMMA;
            case "PERIOD":
                return KeyEvent.VK_PERIOD;
            case "SEMICOLON":
                return KeyEvent.VK_SEMICOLON;
            case "COLON":
                return KeyEvent.VK_COLON;
            case "EXCLAMATION":
                return KeyEvent.VK_EXCLAMATION_MARK;
            case "QUESTION":
                return KeyEvent.VK_SLASH;
            case "AT":
                return KeyEvent.VK_AT;
            case "HASH":
                return KeyEvent.VK_NUMBER_SIGN;
            case "DOLLAR":
                return KeyEvent.VK_4;
            case "PERCENT":
                return KeyEvent.VK_5;
            case "CARET":
                return KeyEvent.VK_6;
            case "AMPERSAND":
                return KeyEvent.VK_7;
            case "ASTERISK":
                return KeyEvent.VK_8;
            case "LEFT_PARENTHESIS":
                return KeyEvent.VK_9;
            case "RIGHT_PARENTHESIS":
                return KeyEvent.VK_0;
            case "MINUS":
                return KeyEvent.VK_MINUS;
            case "PLUS":
                return KeyEvent.VK_EQUALS;
            case "EQUALS":
                return KeyEvent.VK_EQUALS;
            case "LEFT_BRACKET":
                return KeyEvent.VK_OPEN_BRACKET;
            case "RIGHT_BRACKET":
                return KeyEvent.VK_CLOSE_BRACKET;
            case "BACKSLASH":
                return KeyEvent.VK_BACK_SLASH;
            case "FORWARD_SLASH":
                return KeyEvent.VK_SLASH;
            case "PIPE":
                return KeyEvent.VK_BACK_SLASH;
            case "LEFT_BRACE":
                return KeyEvent.VK_OPEN_BRACKET;
            case "RIGHT_BRACE":
                return KeyEvent.VK_CLOSE_BRACKET;
            case "TILDE":
                return KeyEvent.VK_BACK_QUOTE;
            case "BACKTICK":
                return KeyEvent.VK_BACK_QUOTE;
            case "QUOTE":
                return KeyEvent.VK_QUOTE;
            case "DOUBLE_QUOTE":
                return KeyEvent.VK_QUOTEDBL;
            default:
                throw new IllegalArgumentException("Unsupported alphanumeric key: " + key);
        }
    }

    /**
     * Maps the specific key string to its corresponding KeyEvent constant
     * @param key The specific key string
     * @return The corresponding KeyEvent constant
     * @throws IllegalArgumentException if the key is not supported
     */
    public static int getSpecificKeyCode(String key) {
        // Handle single character keys
        if (key.length() == 1) {
            char c = key.charAt(0);
            if (Character.isLetter(c)) {
                return KeyEvent.getExtendedKeyCodeForChar(Character.toUpperCase(c));
            } else if (Character.isDigit(c)) {
                return KeyEvent.getExtendedKeyCodeForChar(c);
            }
        }
        
        // Handle special keys
        switch (key.toLowerCase()) {
            case "space":
                return KeyEvent.VK_SPACE;
            case "backspace":
                return KeyEvent.VK_BACK_SPACE;
            case "delete":
                return KeyEvent.VK_DELETE;
            case "escape":
                return KeyEvent.VK_ESCAPE;
            case "home":
                return KeyEvent.VK_HOME;
            case "end":
                return KeyEvent.VK_END;
            case "page_up":
            case "pageup":
                return KeyEvent.VK_PAGE_UP;
            case "page_down":
            case "pagedown":
                return KeyEvent.VK_PAGE_DOWN;
            case "insert":
                return KeyEvent.VK_INSERT;
            case "f1":
                return KeyEvent.VK_F1;
            case "f2":
                return KeyEvent.VK_F2;
            case "f3":
                return KeyEvent.VK_F3;
            case "f4":
                return KeyEvent.VK_F4;
            case "f5":
                return KeyEvent.VK_F5;
            case "f6":
                return KeyEvent.VK_F6;
            case "f7":
                return KeyEvent.VK_F7;
            case "f8":
                return KeyEvent.VK_F8;
            case "f9":
                return KeyEvent.VK_F9;
            case "f10":
                return KeyEvent.VK_F10;
            case "f11":
                return KeyEvent.VK_F11;
            case "f12":
                return KeyEvent.VK_F12;
            default:
                // Try to get the key code for the character
                if (key.length() == 1) {
                    return KeyEvent.getExtendedKeyCodeForChar(key.charAt(0));
                }
                throw new IllegalArgumentException("Unsupported specific key: " + key);
        }
    }
} 