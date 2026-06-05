package com.testsigma.addons.web;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

@Data
@Action(actionText = "Hover on extracted location x-coordinate and y-coordinate",
        description = "Move the mouse to an already-extracted (x, y) coordinate on the web page and hover. " +
                "Use this after a previous AI step has identified the target location. " +
                "Both coordinates are in CSS viewport pixels.",
        displayName = "Hover on extracted location",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = true)
public class HoverOnExtractedLocation extends WebAction {

    @TestData(reference = "x-coordinate")
    private com.testsigma.sdk.TestData xCoordinate;

    @TestData(reference = "y-coordinate")
    private com.testsigma.sdk.TestData yCoordinate;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final int CROSSHAIR_SIZE  = 12;
    private static final int HIGHLIGHT_STROKE_WIDTH = 2;

    @Override
    public Result execute() {
        logger.info("=== HoverOnExtractedLocation (Web): Starting ===");
        File annotatedFile = null;

        try {
            int x = Integer.parseInt(xCoordinate.getValue().toString().trim());
            int y = Integer.parseInt(yCoordinate.getValue().toString().trim());
            logger.info(String.format("Target coordinates — x=%d  y=%d", x, y));

            // ── Step 1: Hover via Selenium Actions ──
            Actions actions = new Actions(driver);
            actions.moveToLocation(x, y).perform();

            logger.info(String.format("Hover performed at (%d,%d)", x, y));

            // ── Step 2: Capture post-hover screenshot and annotate ──
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage pageCapture = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            BufferedImage annotated = drawCrosshair(pageCapture, x, y);
            annotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "hover_extracted_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, annotatedFile, logger);

            setSuccessMessage(String.format("Successfully hovered at (%d,%d)", x, y));
            return Result.SUCCESS;

        } catch (NumberFormatException e) {
            setErrorMessage("Invalid coordinate value — x and y must be integers. Error: " + e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to hover at extracted location. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            deleteQuietly(annotatedFile);
        }
    }

    // Draws a magenta crosshair and circle at the hover point.
    private BufferedImage drawCrosshair(BufferedImage original, int cx, int cy) {
        BufferedImage copy = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(original, 0, 0, null);

        g.setColor(Color.MAGENTA);
        g.setStroke(new BasicStroke(HIGHLIGHT_STROKE_WIDTH));

        // Horizontal bar
        int x1 = Math.max(0, cx - CROSSHAIR_SIZE);
        int x2 = Math.min(original.getWidth() - 1, cx + CROSSHAIR_SIZE);
        g.drawLine(x1, cy, x2, cy);

        // Vertical bar
        int y1 = Math.max(0, cy - CROSSHAIR_SIZE);
        int y2 = Math.min(original.getHeight() - 1, cy + CROSSHAIR_SIZE);
        g.drawLine(cx, y1, cx, y2);

        // Circle around the point
        int radius = CROSSHAIR_SIZE / 2;
        g.setColor(Color.GREEN);
        g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

        g.dispose();
        return copy;
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
