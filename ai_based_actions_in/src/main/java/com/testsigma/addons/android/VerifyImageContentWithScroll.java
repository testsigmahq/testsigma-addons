package com.testsigma.addons.android;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Data
@Action(actionText = "Ai: Verify if the screen contains or matches the condition verification-query by scrolling through the page",
        description = "Captures 3 sequential screenshots while scrolling down the Android screen and asks AI to verify " +
                "whether the described content or condition is present anywhere across all three views. " +
                "Use this when the content may not be visible in the initial viewport.",
        displayName = "Ai: Verify screen contains (with scroll)",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = true)
public class VerifyImageContentWithScroll extends AndroidAction {

    @TestData(reference = "verification-query")
    private com.testsigma.sdk.TestData verificationQuery;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        logger.info("=== VerifyImageContentWithScroll (Android): Starting ===");
        File screenshot1File    = null;
        File screenshot2File    = null;
        File screenshot3File    = null;
        File finalAnnotatedFile = null;

        try {
            String query = verificationQuery.getValue().toString();
            logger.info("Verification query: " + query);

            // Screenshot 1 — initial view
            BufferedImage capture1 = captureScreen();
            int captureW = capture1.getWidth();
            int captureH = capture1.getHeight();
            logger.info("Screenshot 1 captured: " + captureW + "x" + captureH);
            screenshot1File = AiActionUtils.captureAsJpeg(capture1, "ai_android_scroll_verify_1", logger);

            // Scroll down and take screenshot 2
            scrollDown();
            BufferedImage capture2 = captureScreen();
            logger.info("Screenshot 2 captured after first scroll");
            screenshot2File = AiActionUtils.captureAsJpeg(capture2, "ai_android_scroll_verify_2", logger);

            // Scroll down again and take screenshot 3
            scrollDown();
            BufferedImage capture3 = captureScreen();
            logger.info("Screenshot 3 captured after second scroll");
            screenshot3File = AiActionUtils.captureAsJpeg(capture3, "ai_android_scroll_verify_3", logger);

            // Send all 3 screenshots to AI
            List<File> screenshotFiles = Arrays.asList(screenshot1File, screenshot2File, screenshot3File);
            String aiResponse = AiActionUtils.invokeAiWithFiles(ai, screenshotFiles, AiActionUtils.VERIFY_SCROLL_PROMPT_ANDROID, query, logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get verification response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(capture1, "ai_scroll_verify_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            boolean verified   = responseNode.path("verified").asBoolean(false);
            int imageIndex     = responseNode.path("imageIndex").asInt(1);
            int aiX1           = responseNode.path("x1").asInt(0);
            int aiY1           = responseNode.path("y1").asInt(0);
            int aiX2           = responseNode.path("x2").asInt(0);
            int aiY2           = responseNode.path("y2").asInt(0);
            int imageWidth     = responseNode.path("imageWidth").asInt(0);
            int imageHeight    = responseNode.path("imageHeight").asInt(0);
            int confidence     = responseNode.path("confidence").asInt(0);
            String description = responseNode.path("description").asText("");

            logger.info(String.format(
                    "AI verification result — verified=%b | imageIndex=%d | bbox: (%d,%d)-(%d,%d) | AI image dims: %dx%d | confidence: %d | desc: '%s'",
                    verified, imageIndex, aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, confidence, description));

            // Annotate the screenshot where the content was found
            BufferedImage matchedCapture = imageIndex == 3 ? capture3 : imageIndex == 2 ? capture2 : capture1;
            if (verified && imageWidth > 0 && imageHeight > 0 && (aiX1 | aiY1 | aiX2 | aiY2) != 0) {
                int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
                int capCX = (cap[0] + cap[2]) / 2;
                int capCY = (cap[1] + cap[3]) / 2;
                BufferedImage annotated = AiActionUtils.drawHighlight(
                        matchedCapture, cap[0], cap[1], cap[2], cap[3], capCX, capCY, Color.GREEN);
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_scroll_verify_passed");
            } else {
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(matchedCapture, "ai_scroll_verify_result");
            }
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            if (verified) {
                setSuccessMessage(String.format(
                        "Verification PASSED for '%s' in screenshot %d | confidence=%d | %s",
                        query, imageIndex, confidence, description));
                return Result.SUCCESS;
            } else {
                setErrorMessage(String.format(
                        "Verification FAILED for '%s' across all 3 screenshots | confidence=%d | %s",
                        query, confidence, description));
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to verify using AI with scroll. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshot1File);
            AiActionUtils.deleteQuietly(screenshot2File);
            AiActionUtils.deleteQuietly(screenshot3File);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }

    private BufferedImage captureScreen() throws Exception {
        byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    private void scrollDown() throws InterruptedException {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        org.openqa.selenium.Dimension size = appiumDriver.manage().window().getSize();
        int startX = (int) (size.width * 0.50);
        int startY = (int) (size.height * 0.90);
        int endY   = (int) (size.height * 0.10);
        logger.info(String.format("Scrolling: window size=%dx%d, swipe (%d,%d)->(%d,%d)",
                size.width, size.height, startX, startY, startX, endY));
        PointerInput finger = new PointerInput(TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), viewport(), startX, startY))
                .addAction(finger.createPointerDown(LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofSeconds(2), viewport(), startX, endY))
                .addAction(finger.createPointerUp(LEFT.asArg()));
        appiumDriver.perform(Collections.singletonList(swipe));
        Thread.sleep(800);
    }
}
