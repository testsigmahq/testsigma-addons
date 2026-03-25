package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.KeyboardUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import com.testsigma.sdk.Result;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@lombok.EqualsAndHashCode(callSuper = false)
@Action(actionText = "Perform Keyboard actions actions-to-perform",
        description = "This action performs multiple keyboard actions in sequence. " +
                "Supports text copy-paste, special keys like {TAB}, {ENTER}, individual character typing with {KEY(xyz)}, and wait delays with {WAIT(n)}. " +
                "Example: {WAIT(1)}test123{TAB}{WAIT(1)}abc{ENTER}{KEY(xyz)}{WAIT(2)}",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Perform Keyboard actions",
        useCustomScreenshot = true)
public class PerformMultipleActions extends WindowsAdvancedAction {

    @TestData(reference = "actions-to-perform")
    private com.testsigma.sdk.TestData actionsToPerform;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    // Pattern to match any delimiter: {KEY(xyz)}, {WAIT(n)}, or {SPECIALKEY}
    private static final Pattern DELIMITER_PATTERN = Pattern.compile("\\{(?:KEY\\([^)]+\\)|WAIT\\([^)]+\\)|[^}]+)\\}");

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            logger.info("=== Perform Multiple Actions: Starting Execution ===");

            String actionsToPerformValue = actionsToPerform.getValue().toString();
            logger.info("Actions to perform: " + actionsToPerformValue);

            // Instantiate the Robot Class
            Robot robot = new Robot();

            // Wait 100ms to ensure focus
            Thread.sleep(100);

            // Parse and execute actions in sequential order
            parseAndExecuteActions(robot, actionsToPerformValue);

            setSuccessMessage("All keyboard actions performed successfully");
            logger.info("Successfully completed all keyboard actions");

            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "perform_multiple_actions_screenshot", logger);

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("An error occurred while performing keyboard actions: " + e.getMessage());
            logger.debug("Error performing keyboard actions: " + ExceptionUtils.getStackTrace(e));
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "perform_multiple_actions_failure_screenshot", logger);
        }
        return result;
    }

    /**
     * Parse the input string and execute the corresponding actions in sequence
     */
    private void parseAndExecuteActions(Robot robot, String input) throws Exception {
        Matcher matcher = DELIMITER_PATTERN.matcher(input);
        int lastEnd = 0;

        logger.info("=== Starting Sequential Execution ===");

        while (matcher.find()) {
            ;
            // Process text segment before this delimiter
            if (matcher.start() > lastEnd) {
                String textSegment = input.substring(lastEnd, matcher.start());
                if (!textSegment.isEmpty()) {
                    logger.info("Copy-pasting text: '" + textSegment + "'");
                    copyAndPasteText(robot, textSegment);
                }
            }

            // Process the delimiter
            String delimiter = matcher.group();
            logger.info("Processing delimiter: " + delimiter);
            processDelimiter(robot, delimiter);

            lastEnd = matcher.end();
        }

        // Process any remaining text after the last delimiter
        if (lastEnd < input.length()) {
            String textSegment = input.substring(lastEnd);
            if (!textSegment.isEmpty()) {
                logger.info("Copy-pasting remaining text: '" + textSegment + "'");
                copyAndPasteText(robot, textSegment);
            }
        }

        logger.info("=== Sequential Execution Complete ===");
    }

    /**
     * Process a delimiter (special key, KEY pattern, or WAIT pattern)
     */
    private void processDelimiter(Robot robot, String delimiter) throws Exception {
        // Handle {WAIT(n)} pattern
        if (delimiter.matches("\\{WAIT\\([^)]+\\)\\}")) {
            String waitTime = delimiter.substring(6, delimiter.length() - 2); // Extract n from {WAIT(n)}
            logger.info("Waiting for: " + waitTime + " seconds");
            handleWait(waitTime);
        }
        // Handle {KEY(xyz)} pattern
        else if (delimiter.matches("\\{KEY\\([^)]+\\)\\}")) {
            String characters = delimiter.substring(5, delimiter.length() - 2); // Extract xyz from {KEY(xyz)}
            logger.info("Typing individually: '" + characters + "'");
            typeCharactersIndividually(robot, characters);
        }
        // Handle special keys like {TAB}, {ENTER} (but not {WAIT(n)} or {KEY(xyz)})
        else if (delimiter.matches("\\{[^}()]+\\}")) {
            String keyName = delimiter.substring(1, delimiter.length() - 1);
            logger.info("Pressing special key: " + keyName);
            pressSpecialKey(robot, keyName);
            Thread.sleep(300);
        }
    }

    /**
     * Handle wait functionality for {WAIT(n)} pattern
     */
    private void handleWait(String waitTime) throws Exception {
        try {
            // Parse the wait time as integer (seconds)
            int seconds = Integer.parseInt(waitTime.trim());
            
            if (seconds < 0) {
                throw new IllegalArgumentException("Wait time cannot be negative: " + seconds);
            }
            
            // Convert seconds to milliseconds and sleep
            long milliseconds = seconds * 1000L;
            logger.info("Sleeping for " + seconds + " seconds (" + milliseconds + " ms)");
            Thread.sleep(milliseconds);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid wait time format: " + waitTime + ". Expected integer value.");
        }
    }

    /**
     * Copy and paste text using clipboard (Windows: Ctrl+V)
     */
    private void copyAndPasteText(Robot robot, String text) throws Exception {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable originalContent = null;

        try {
            // Save current clipboard (optional - ignore if fails)
            try {
                originalContent = clipboard.getContents(null);
            } catch (Exception e) {
                logger.debug("Could not save original clipboard content: " + e.getMessage());
            }

            // Set text to clipboard
            StringSelection stringSelection = new StringSelection(text);
            clipboard.setContents(stringSelection, null);
            Thread.sleep(100);

            // Paste using Ctrl+V (Windows)
            robot.keyPress(KeyEvent.VK_CONTROL);
            KeyboardUtils.sleep(50);
            robot.keyPress(KeyEvent.VK_V);
            KeyboardUtils.sleep(50);
            robot.keyRelease(KeyEvent.VK_V);
            KeyboardUtils.sleep(50);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            Thread.sleep(100);

        } finally {
            // Restore original clipboard content
            if (originalContent != null) {
                try {
                    clipboard.setContents(originalContent, null);
                } catch (Exception e) {
                    logger.debug("Could not restore original clipboard content: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Type characters individually
     */
    private void typeCharactersIndividually(Robot robot, String characters) throws Exception {
        for (char c : characters.toCharArray()) {
            typeCharacter(robot, c);
            Thread.sleep(50);
        }
    }

    /**
     * Type a single character
     */
    private void typeCharacter(Robot robot, char character) throws Exception {
        boolean upperCase = Character.isUpperCase(character);
        boolean needsShift = false;
        int keyCode;

        // Handle special characters that need shift
        switch (character) {
            case '!':
                keyCode = KeyEvent.VK_1;
                needsShift = true;
                break;
            case '@':
                keyCode = KeyEvent.VK_2;
                needsShift = true;
                break;
            case '#':
                keyCode = KeyEvent.VK_3;
                needsShift = true;
                break;
            case '$':
                keyCode = KeyEvent.VK_4;
                needsShift = true;
                break;
            case '%':
                keyCode = KeyEvent.VK_5;
                needsShift = true;
                break;
            case '^':
                keyCode = KeyEvent.VK_6;
                needsShift = true;
                break;
            case '&':
                keyCode = KeyEvent.VK_7;
                needsShift = true;
                break;
            case '*':
                keyCode = KeyEvent.VK_8;
                needsShift = true;
                break;
            case '(':
                keyCode = KeyEvent.VK_9;
                needsShift = true;
                break;
            case ')':
                keyCode = KeyEvent.VK_0;
                needsShift = true;
                break;
            case '_':
                keyCode = KeyEvent.VK_MINUS;
                needsShift = true;
                break;
            case '+':
                keyCode = KeyEvent.VK_EQUALS;
                needsShift = true;
                break;
            case '{':
                keyCode = KeyEvent.VK_OPEN_BRACKET;
                needsShift = true;
                break;
            case '}':
                keyCode = KeyEvent.VK_CLOSE_BRACKET;
                needsShift = true;
                break;
            case '|':
                keyCode = KeyEvent.VK_BACK_SLASH;
                needsShift = true;
                break;
            case ':':
                keyCode = KeyEvent.VK_SEMICOLON;
                needsShift = true;
                break;
            case '"':
                keyCode = KeyEvent.VK_QUOTE;
                needsShift = true;
                break;
            case '<':
                keyCode = KeyEvent.VK_COMMA;
                needsShift = true;
                break;
            case '>':
                keyCode = KeyEvent.VK_PERIOD;
                needsShift = true;
                break;
            case '?':
                keyCode = KeyEvent.VK_SLASH;
                needsShift = true;
                break;
            case '~':
                keyCode = KeyEvent.VK_BACK_QUOTE;
                needsShift = true;
                break;
            default:
                keyCode = KeyEvent.getExtendedKeyCodeForChar(character);
                if (keyCode == KeyEvent.VK_UNDEFINED) {
                    throw new IllegalArgumentException("Cannot type character: " + character);
                }
                needsShift = upperCase;
        }

        if (needsShift || upperCase) {
            robot.keyPress(KeyEvent.VK_SHIFT);
            KeyboardUtils.sleep(10);
        }

        robot.keyPress(keyCode);
        KeyboardUtils.sleep(10);
        robot.keyRelease(keyCode);

        if (needsShift || upperCase) {
            KeyboardUtils.sleep(10);
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }

    /**
     * Press a special key
     */
    private void pressSpecialKey(Robot robot, String keyName) throws Exception {
        int keyCode = getSpecialKeyCode(keyName);

        robot.keyPress(keyCode);
        KeyboardUtils.sleep(30);
        robot.keyRelease(keyCode);
        Thread.sleep(50);
    }

    /**
     * Get the key code for special keys
     */
    private int getSpecialKeyCode(String keyName) {
        switch (keyName.toUpperCase()) {
            case "TAB":
                return KeyEvent.VK_TAB;
            case "ENTER":
                return KeyEvent.VK_ENTER;
            case "SPACE":
                return KeyEvent.VK_SPACE;
            case "BACKSPACE":
                return KeyEvent.VK_BACK_SPACE;
            case "DELETE":
                return KeyEvent.VK_DELETE;
            case "WINDOWS":
            case "WIN":
            case "WINDOW":
                return KeyEvent.VK_WINDOWS;
            case "ESC":
            case "ESCAPE":
                return KeyEvent.VK_ESCAPE;
            case "UP":
                return KeyEvent.VK_UP;
            case "DOWN":
                return KeyEvent.VK_DOWN;
            case "LEFT":
                return KeyEvent.VK_LEFT;
            case "RIGHT":
                return KeyEvent.VK_RIGHT;
            case "HOME":
                return KeyEvent.VK_HOME;
            case "END":
                return KeyEvent.VK_END;
            case "PAGE_UP":
                return KeyEvent.VK_PAGE_UP;
            case "PAGE_DOWN":
                return KeyEvent.VK_PAGE_DOWN;
            case "INSERT":
                return KeyEvent.VK_INSERT;
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
            default:
                throw new IllegalArgumentException("Unsupported special key: " + keyName);
        }
    }
}