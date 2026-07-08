package com.testsigma.addons.windows;

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
import java.util.HashSet;
import java.util.Set;

@Data
@Action(actionText = "Ai: Verify if the pdf at pdf-path on page page-number contains or matches the condition verification-query",
        description = "Renders the specified page of the PDF to an image and asks AI to verify whether the described " +
                "content or condition is present on that page. The step passes if AI confirms the query; fails otherwise. " +
                "pdf-path may be a local file path or an http(s) URL.",
        displayName = "Ai: Verify PDF page contains",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class VerifyPdfContentAtGivenPage extends WindowsAction {

    @TestData(reference = "pdf-path")
    private com.testsigma.sdk.TestData pdfPath;

    @TestData(reference = "page-number")
    private com.testsigma.sdk.TestData pageNumber;

    @TestData(reference = "verification-query")
    private com.testsigma.sdk.TestData verificationQuery;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final int RENDER_DPI = 150;

    @Override
    public Result execute() {
        logger.info("=== VerifyPdfContentAtGivenPage: Starting ===");

        PDDocument doc          = null;
        File screenshotFile     = null;
        File finalAnnotatedFile = null;
        Set<File> tempFiles     = new HashSet<>();

        try {
            String path    = pdfPath.getValue().toString().trim();
            String query   = verificationQuery.getValue().toString();
            String pageStr = pageNumber.getValue().toString().trim();

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

            logger.info("PDF: " + path + " | page: " + page + " | verification query: " + query);

            File pdfFile = resolveFile("input.pdf", path, tempFiles);
            doc = Loader.loadPDF(pdfFile);

            int totalPages = doc.getNumberOfPages();
            logger.info("PDF pages: " + totalPages);
            if (page > totalPages) {
                setErrorMessage(String.format(
                        "Page %d does not exist in PDF — it only has %d page(s)", page, totalPages));
                return Result.FAILED;
            }

            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage pageCapture = renderer.renderImageWithDPI(page - 1, RENDER_DPI);
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Rendered page size: " + captureW + "x" + captureH);

            screenshotFile = AiActionUtils.captureAsJpeg(pageCapture, "ai_pdf_verify_capture_p" + page, logger);

            String aiResponse = AiActionUtils.invokeAi(ai, screenshotFile, AiActionUtils.VERIFY_PROMPT_PDF, query,
                    logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get verification response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_pdf_verify_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            boolean verified   = responseNode.path("verified").asBoolean(false);
            int aiX1           = responseNode.path("x1").asInt(0);
            int aiY1           = responseNode.path("y1").asInt(0);
            int aiX2           = responseNode.path("x2").asInt(0);
            int aiY2           = responseNode.path("y2").asInt(0);
            int imageWidth     = responseNode.path("imageWidth").asInt(0);
            int imageHeight    = responseNode.path("imageHeight").asInt(0);
            int confidence     = responseNode.path("confidence").asInt(0);
            String description = responseNode.path("description").asText("");

            logger.info(String.format(
                    "AI verification result — verified=%b | bbox: (%d,%d)-(%d,%d) | AI image dims: %dx%d | confidence: %d | desc: '%s'",
                    verified, aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, confidence, description));

            if (verified && imageWidth > 0 && imageHeight > 0 && (aiX1 | aiY1 | aiX2 | aiY2) != 0) {
                int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
                int capCX = (cap[0] + cap[2]) / 2;
                int capCY = (cap[1] + cap[3]) / 2;
                BufferedImage annotated = AiActionUtils.drawHighlight(
                        pageCapture, cap[0], cap[1], cap[2], cap[3], capCX, capCY, Color.GREEN);
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_pdf_verify_passed");
            } else {
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_pdf_verify_result");
            }
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            if (verified) {
                setSuccessMessage(String.format(
                        "Successfully verified that the given condition is met on page %d for '%s' | confidence=%d | %s",
                        page, query, confidence, description));
                return Result.SUCCESS;
            } else {
                setErrorMessage(String.format(
                        "Verification FAILED on page %d for '%s' | confidence=%d | %s",
                        page, query, confidence, description));
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to verify PDF content using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            closeSilently(doc);
            tempFiles.forEach(AiActionUtils::deleteQuietly);
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
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

    private void closeSilently(PDDocument doc) {
        if (doc != null) {
            try { doc.close(); } catch (Exception ignored) {}
        }
    }
}
