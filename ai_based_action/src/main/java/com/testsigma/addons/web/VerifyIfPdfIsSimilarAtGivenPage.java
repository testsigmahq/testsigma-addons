package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Action(actionText = "Verify if pdf at actual-pdf-path is similar to base pdf at base-pdf-path on page page-number using prompt additional-prompt",
        description = "Renders the specified page of both PDFs to images and uses AI to compare them. " +
                "Use additional-prompt to specify regions to ignore or aspects to focus on (e.g. 'ignore the footer'). " +
                "Fails if AI detects meaningful differences on that page.",
        displayName = "Ai: Verify PDF page similarity",
        applicationType = com.testsigma.sdk.ApplicationType.WEB,
        useCustomScreenshot = true)
public class VerifyIfPdfIsSimilarAtGivenPage extends WebAction {

    @TestData(reference = "base-pdf-path")
    private com.testsigma.sdk.TestData basePdfPath;

    @TestData(reference = "actual-pdf-path")
    private com.testsigma.sdk.TestData actualPdfPath;

    @TestData(reference = "page-number")
    private com.testsigma.sdk.TestData pageNumber;

    @TestData(reference = "additional-prompt")
    private com.testsigma.sdk.TestData additionalPrompt;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final int RENDER_DPI = 150;

    @Override
    public Result execute() {
        logger.info("=== VerifyIfPdfIsSimilarAtGivenPage: Starting ===");

        PDDocument baseDoc   = null;
        PDDocument actualDoc = null;
        File finalScreenshot = null;
        Set<File> tempFiles  = new HashSet<>();

        try {
            String basePath   = basePdfPath.getValue().toString().trim();
            String actualPath = actualPdfPath.getValue().toString().trim();
            String prompt     = (additionalPrompt != null && additionalPrompt.getValue() != null)
                    ? additionalPrompt.getValue().toString().trim() : "";
            String pageStr    = pageNumber.getValue().toString().trim();

            int page;
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                setErrorMessage("Invalid page number '" + pageStr + "': must be a positive integer");
                return Result.FAILED;
            }
            if (page < 1) {
                setErrorMessage("Page number must be >= 1, got: " + page);
                return Result.FAILED;
            }

            logger.info("Base PDF: " + basePath);
            logger.info("Actual PDF: " + actualPath);
            logger.info("Page number: " + page);
            logger.info("Additional prompt: " + prompt);

            File baseFile   = resolveFile("base.pdf",   basePath,   tempFiles);
            File actualFile = resolveFile("actual.pdf", actualPath, tempFiles);

            baseDoc   = Loader.loadPDF(baseFile);
            actualDoc = Loader.loadPDF(actualFile);

            int basePages   = baseDoc.getNumberOfPages();
            int actualPages = actualDoc.getNumberOfPages();
            logger.info("Base pages: " + basePages + " | Actual pages: " + actualPages);

            if (page > basePages) {
                setErrorMessage(String.format(
                        "Page %d does not exist in base PDF — it only has %d page(s)", page, basePages));
                return Result.FAILED;
            }
            if (page > actualPages) {
                setErrorMessage(String.format(
                        "Page %d does not exist in actual PDF — it only has %d page(s)", page, actualPages));
                return Result.FAILED;
            }

            int zeroIndex = page - 1;
            PDFRenderer baseRenderer   = new PDFRenderer(baseDoc);
            PDFRenderer actualRenderer = new PDFRenderer(actualDoc);

            BufferedImage baseImg   = baseRenderer.renderImageWithDPI(zeroIndex, RENDER_DPI);
            BufferedImage actualImg = actualRenderer.renderImageWithDPI(zeroIndex, RENDER_DPI);

            File baseJpeg   = AiActionUtils.captureAsJpeg(baseImg,   "ai_pdf_base_p"   + page, logger);
            File actualJpeg = AiActionUtils.captureAsJpeg(actualImg, "ai_pdf_actual_p" + page, logger);

            try {
                List<File> files = Arrays.asList(baseJpeg, actualJpeg);

                String aiResponse = AiActionUtils.invokeAiWithFiles(ai, files, AiActionUtils.COMPARE_PDF_PROMPT, prompt, logger);

                JsonNode node = AiActionUtils.parseAiJson(aiResponse, logger);
                if (node == null) {
                    setErrorMessage("Failed to get AI comparison response for page " + page + " (contact support)");
                    finalScreenshot = ScreenshotUtils.saveScreenshotToFile(actualImg, "ai_pdf_error_p" + page);
                    ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalScreenshot, logger);
                    return Result.FAILED;
                }

                boolean match      = node.path("match").asBoolean(false);
                int confidence     = node.path("confidence").asInt(0);
                String description = node.path("description").asText("");
                JsonNode diffsNode = node.path("differences");

                logger.info(String.format("Page %d — match=%b | confidence=%d | desc='%s'",
                        page, match, confidence, description));

                if (!match) {
                    StringBuilder diffs = new StringBuilder();
                    if (diffsNode.isArray()) {
                        diffsNode.forEach(d -> diffs.append("\n• ").append(d.asText()));
                    }
                    setErrorMessage(String.format(
                            "Differences found on page %d | confidence=%d | %s%s",
                            page, confidence, description, diffs));
                    int aiWidth      = node.path("imageWidth").asInt(0);
                    int aiHeight     = node.path("imageHeight").asInt(0);
                    JsonNode regions = node.path("regions");
                    finalScreenshot = ScreenshotUtils.saveScreenshotToFile(
                            buildSideBySide(baseImg, actualImg, regions, aiWidth, aiHeight, page), "ai_pdf_diff_p" + page);
                    ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalScreenshot, logger);
                    return Result.FAILED;
                }
            } finally {
                AiActionUtils.deleteQuietly(baseJpeg);
                AiActionUtils.deleteQuietly(actualJpeg);
            }

            finalScreenshot = ScreenshotUtils.saveScreenshotToFile(actualImg, "ai_pdf_passed_p" + page);
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalScreenshot, logger);

            setSuccessMessage(String.format(
                    "AI verified both PDFs are similar on page %d", page));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to compare PDFs using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            closeSilently(baseDoc);
            closeSilently(actualDoc);
            tempFiles.forEach(AiActionUtils::deleteQuietly);
            AiActionUtils.deleteQuietly(finalScreenshot);
        }
    }

    private File resolveFile(String tempName, String path, Set<File> tempFiles) throws Exception {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            String base = tempName.substring(0, tempName.lastIndexOf('.'));
            String ext  = tempName.substring(tempName.lastIndexOf('.'));
            File tmp = File.createTempFile(base, ext);
            try (InputStream in = new URL(path).openStream()) {
                Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("Downloaded " + path + " → " + tmp.getAbsolutePath());
            tempFiles.add(tmp);
            return tmp;
        }
        return new File(path);
    }

    private BufferedImage buildSideBySide(BufferedImage base, BufferedImage actual,
                                           JsonNode regions, int aiWidth, int aiHeight, int page) {
        BufferedImage annotatedBase   = drawDiffHighlights(base,   regions, aiWidth, aiHeight);
        BufferedImage annotatedActual = drawDiffHighlights(actual, regions, aiWidth, aiHeight);
        int labelHeight = 30;
        int gap         = 10;
        int w = annotatedBase.getWidth() + annotatedActual.getWidth() + gap;
        int h = Math.max(annotatedBase.getHeight(), annotatedActual.getHeight()) + labelHeight;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("BASE  (page " + page + ")", 4, 20);
        g.drawString("ACTUAL  (page " + page + ")", annotatedBase.getWidth() + gap + 4, 20);
        g.drawImage(annotatedBase,   0,                              labelHeight, null);
        g.drawImage(annotatedActual, annotatedBase.getWidth() + gap, labelHeight, null);
        g.dispose();
        return out;
    }

    private BufferedImage drawDiffHighlights(BufferedImage src, JsonNode regions, int aiWidth, int aiHeight) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, null);
        if (regions != null && regions.isArray() && aiWidth > 0 && aiHeight > 0) {
            for (JsonNode region : regions) {
                int aiX1 = region.path("x1").asInt(0);
                int aiY1 = region.path("y1").asInt(0);
                int aiX2 = region.path("x2").asInt(0);
                int aiY2 = region.path("y2").asInt(0);
                if (aiX2 > aiX1 && aiY2 > aiY1) {
                    int[] cap = AiActionUtils.scaleAiToCapture(
                            aiX1, aiY1, aiX2, aiY2, aiWidth, aiHeight, src.getWidth(), src.getHeight());
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                    g.setColor(Color.RED);
                    g.fillRect(cap[0], cap[1], cap[2] - cap[0], cap[3] - cap[1]);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g.setStroke(new BasicStroke(2));
                    g.drawRect(cap[0], cap[1], cap[2] - cap[0], cap[3] - cap[1]);
                }
            }
        }
        g.dispose();
        return copy;
    }

    private void closeSilently(PDDocument doc) {
        if (doc != null) {
            try { doc.close(); } catch (Exception ignored) {}
        }
    }
}
