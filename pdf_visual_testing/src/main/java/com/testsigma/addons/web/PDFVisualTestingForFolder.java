package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.web.util.Constants;
import com.testsigma.addons.web.util.Coordinate;
import com.testsigma.addons.web.util.ResponseObject;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.openqa.selenium.NoSuchElementException;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Action(actionText = "Perform visual testing on all pdf files in base folder base-folder-path against the" +
        " pdf files in actual folder actual-folder-path, pairing files by pairing-strategy",
        description = "Iterates over every pdf file in the base folder and its corresponding pdf file in the" +
                " actual folder (paired either by matching filename or by matching sorted position, controlled" +
                " by pairing-strategy), performs page-by-page visual testing for each pair, and generates a" +
                " single consolidated pdf report containing side-by-side comparison images for every page" +
                " checked. The local path of this report is included in both the success and the error" +
                " message, along with the number of files that passed and the number that failed.",
        displayName = "PDF Visual Testing For Folder",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class PDFVisualTestingForFolder extends WebAction {

    @TestData(reference = "base-folder-path")
    private com.testsigma.sdk.TestData baseFolderPath_;

    @TestData(reference = "actual-folder-path")
    private com.testsigma.sdk.TestData actualFolderPath_;

    @TestData(reference = "pairing-strategy", allowedValues = {"filename", "position"})
    private com.testsigma.sdk.TestData pairingStrategy_;

    // Number of pages compared concurrently per file. The remote visual-comparison API call is
    // a network round trip and dominates page processing time; performing them one at a time
    // per page serializes hundreds/thousands of round trips. PDF rendering itself stays
    // single-threaded (PDFRenderer/PDDocument are not safe for concurrent use).
    private static final int PAGE_CONCURRENCY = 10;

    private static final ObjectMapper mapper = new ObjectMapper();

    // Shared, connection-pooled HTTP client reused across every page/file so pages benefit from
    // HTTP keep-alive instead of paying a fresh TCP+TLS handshake on every single page.
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectionPool(new okhttp3.ConnectionPool(32, 5, TimeUnit.MINUTES))
            .build();

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution for PDFVisualTestingForFolder action");
        String baseFolderPath = baseFolderPath_.getValue().toString();
        String actualFolderPath = actualFolderPath_.getValue().toString();
        String pairingStrategy = pairingStrategy_.getValue().toString();
        logger.info("Base folder path: " + baseFolderPath + ", Actual folder path: " + actualFolderPath +
                ", Pairing strategy: " + pairingStrategy);

        int filesPassed = 0;
        int filesFailed = 0;
        StringBuilder failureDetails = new StringBuilder();
        String setupError = null;

        // Bounded queue + CallerRunsPolicy: once PAGE_CONCURRENCY*2 page-comparisons are queued,
        // the calling thread runs the next one itself instead of racing ahead of the network
        // and piling up rendered pages/temp PNGs in memory.
        ExecutorService pageExecutor = new ThreadPoolExecutor(PAGE_CONCURRENCY, PAGE_CONCURRENCY,
                60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(PAGE_CONCURRENCY * 2),
                new ThreadPoolExecutor.CallerRunsPolicy());

        File reportFile;
        try (PDDocument reportDocument = new PDDocument(IOUtils.createTempFileOnlyStreamCache())) {
            try {
                File baseFolder = new File(baseFolderPath);
                File actualFolder = new File(actualFolderPath);
                logger.info("checking if base folder and actual folder exist and are directories");

                if (!baseFolder.isDirectory()) {
                    setupError = "Base folder does not exist or is not a directory : " + baseFolderPath;
                } else if (!actualFolder.isDirectory()) {
                    setupError = "Actual folder does not exist or is not a directory : " + actualFolderPath;
                } else {
                    List<String> unmatchedBaseFiles = new ArrayList<>();
                    logger.info("Pairing files in base folder and actual folder using pairing strategy: " + pairingStrategy);
                    Map<File, File> pairs = pairFiles(baseFolder, actualFolder, pairingStrategy, unmatchedBaseFiles);
                    int totalFilePairs = pairs.size();
                    logger.info(String.format("Found %d matched pdf file pair(s) to compare and %d unmatched" +
                            " file(s) in the base folder", totalFilePairs, unmatchedBaseFiles.size()));

                    for (String unmatched : unmatchedBaseFiles) {
                        filesFailed++;
                        failureDetails.append(unmatched).append(" - no matching file found in actual folder; ");
                        addMessagePageToReport(reportDocument,
                                unmatched + " - FAILED (no matching file found in actual folder)");
                    }

                    int fileIndex = 0;
                    for (Map.Entry<File, File> entry : pairs.entrySet()) {
                        fileIndex++;
                        logger.info(String.format("Iterating file %d/%d : %s", fileIndex, totalFilePairs,
                                entry.getKey().getName()));
                        boolean filePassed = compareSingleFile(entry.getKey(), entry.getValue(), fileIndex,
                                totalFilePairs, reportDocument, failureDetails, pageExecutor);
                        if (filePassed) {
                            filesPassed++;
                        } else {
                            filesFailed++;
                        }
                    }
                }
            } catch (OutOfMemoryError oom) {
                logger.info("OutOfMemoryError while processing base/actual folders : " + ExceptionUtils.getStackTrace(oom));
                setupError = (setupError == null ? "" : setupError + "; ") + "Visual testing failed due to the" +
                        " JVM running out of memory (OutOfMemoryError) while processing the folders. Try" +
                        " increasing the JVM heap size (-Xmx) for the agent, lowering the render DPI, or" +
                        " processing fewer files per run.";
            } catch (Exception e) {
                logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
                setupError = (setupError == null ? "" : setupError + "; ") + "Unexpected error: " + e.getMessage();
            }

            if (setupError != null) {
                addMessagePageToReport(reportDocument, setupError);
            }
            if (reportDocument.getNumberOfPages() == 0) {
                addMessagePageToReport(reportDocument, "No pdf files found to compare in the given folders.");
            }

            reportFile = File.createTempFile("pdf_visual_testing_report_", ".pdf");
            reportDocument.save(reportFile);
        } catch (OutOfMemoryError oom) {
            logger.info("OutOfMemoryError while building the consolidated report pdf : " + ExceptionUtils.getStackTrace(oom));
            setErrorMessage("Unable to generate the consolidated visual testing report pdf because the JVM ran" +
                    " out of memory (OutOfMemoryError). Try increasing the JVM heap size (-Xmx) for the agent" +
                    " or lowering the render DPI.");
            pageExecutor.shutdownNow();
            return Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception while building the consolidated report pdf : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to generate the consolidated visual testing report pdf : " + e.getMessage());
            pageExecutor.shutdownNow();
            return Result.FAILED;
        } finally {
            pageExecutor.shutdown();
            try {
                pageExecutor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        String reportLink = buildReportLink(reportFile);

        if (setupError != null) {
            setErrorMessage(setupError + ". Consolidated report: <b>" + reportLink + "</b>");
            return Result.FAILED;
        }

        int totalFiles = filesPassed + filesFailed;
        if (filesFailed == 0) {
            setSuccessMessage(String.format("Visual testing completed for all <b>%d</b> file(s) - <b>%d passed</b>," +
                            " <b>%d failed</b>. Consolidated report: <b>%s</b>",
                    totalFiles, filesPassed, filesFailed, reportLink));
            return Result.SUCCESS;
        } else {
            setErrorMessage(String.format("Visual testing completed for <b>%d</b> file(s) - <b>%d passed</b>," +
                            " <b>%d failed</b>. Consolidated report: <b>%s</b>. Failures: %s",
                    totalFiles, filesPassed, filesFailed, reportLink, failureDetails));
            return Result.FAILED;
        }
    }

    private String buildReportLink(File reportFile) {
        String url = reportFile.toURI().toString();
        return String.format("<a href=\"%s\" target=\"_blank\" rel=\"noopener noreferrer\">%s</a>",
                url, reportFile.getAbsolutePath());
    }

    private Map<File, File> pairFiles(File baseFolder, File actualFolder, String pairingStrategy,
                                      List<String> unmatchedBaseFiles) {
        File[] baseFilesArr = baseFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        File[] actualFilesArr = actualFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        List<File> baseFiles = new ArrayList<>(baseFilesArr == null ? Collections.emptyList() : java.util.Arrays.asList(baseFilesArr));
        List<File> actualFiles = new ArrayList<>(actualFilesArr == null ? Collections.emptyList() : java.util.Arrays.asList(actualFilesArr));
        baseFiles.sort(Comparator.comparing(File::getName));
        actualFiles.sort(Comparator.comparing(File::getName));

        Map<File, File> pairs = new LinkedHashMap<>();
        if ("position".equalsIgnoreCase(pairingStrategy)) {
            int count = Math.min(baseFiles.size(), actualFiles.size());
            for (int i = 0; i < count; i++) {
                pairs.put(baseFiles.get(i), actualFiles.get(i));
            }
            for (int i = count; i < baseFiles.size(); i++) {
                unmatchedBaseFiles.add(baseFiles.get(i).getName());
            }
        } else {
            Map<String, File> actualByName = new HashMap<>();
            for (File f : actualFiles) {
                actualByName.put(f.getName(), f);
            }
            for (File base : baseFiles) {
                File match = actualByName.get(base.getName());
                if (match != null) {
                    pairs.put(base, match);
                } else {
                    unmatchedBaseFiles.add(base.getName());
                }
            }
        }
        return pairs;
    }

    private boolean compareSingleFile(File basePdf, File actualPdf, int fileIndex, int totalFiles,
                                      PDDocument reportDocument, StringBuilder failureDetails,
                                      ExecutorService pageExecutor) {
        String fileLabel = basePdf.getName();
        int currentPage = 0;
        int pagesToCompare = 0;
        try (PDDocument baseDocument = Loader.loadPDF(basePdf);
             PDDocument actualDocument = Loader.loadPDF(actualPdf)) {
            int basePageCount = baseDocument.getNumberOfPages();
            int actualPageCount = actualDocument.getNumberOfPages();
            pagesToCompare = Math.min(basePageCount, actualPageCount);
            int totalPagesToCompare = pagesToCompare;
            boolean pageCountMismatch = basePageCount != actualPageCount;
            AtomicBoolean allPagesPassed = new AtomicBoolean(true);

            logger.info(String.format("File %d/%d (%s) - %d page(s) to compare (base has %d page(s), actual has" +
                    " %d page(s))", fileIndex, totalFiles, fileLabel, pagesToCompare, basePageCount, actualPageCount));

            // PDFRenderer/PDDocument are not safe for concurrent use, so rendering stays on this
            // single thread. The remote comparison API call (the actual bottleneck - a network
            // round trip) and writing the resulting page into the shared report run concurrently.
            PDFRenderer baseRenderer = new PDFRenderer(baseDocument);
            PDFRenderer actualRenderer = new PDFRenderer(actualDocument);

            List<Future<Void>> pageFutures = new ArrayList<>(pagesToCompare);
            for (int page = 1; page <= pagesToCompare; page++) {
                currentPage = page;
                int pageNumber = page;
                logger.info(String.format("File %d/%d (%s) - rendering page %d/%d", fileIndex, totalFiles,
                        fileLabel, page, pagesToCompare));
                BufferedImage baseImage = baseRenderer.renderImageWithDPI(page - 1, 150);
                BufferedImage actualImage = actualRenderer.renderImageWithDPI(page - 1, 150);

                Callable<Void> pageTask = () -> {
                    try {
                        File baseTemp = File.createTempFile("base_page_", ".png");
                        File actualTemp = File.createTempFile("actual_page_", ".png");
                        try {
                            ImageIO.write(baseImage, "png", baseTemp);
                            ImageIO.write(actualImage, "png", actualTemp);

                            boolean pagePassed;
                            List<Coordinate> diffCoordinates;
                            try {
                                ResponseObject responseObject = performApiCall(baseTemp, actualTemp, pageNumber);
                                pagePassed = responseObject.getPer_similar() == 1 && responseObject.getDiff_coordinates().isEmpty();
                                diffCoordinates = pagePassed ? Collections.emptyList() : responseObject.getDiff_coordinates();
                            } catch (Exception apiError) {
                                logger.info(String.format("File %d/%d (%s) - page %d - API call failed : %s",
                                        fileIndex, totalFiles, fileLabel, pageNumber, ExceptionUtils.getStackTrace(apiError)));
                                pagePassed = false;
                                diffCoordinates = Collections.emptyList();
                            }

                            String header = String.format("%s - page %d/%d - %s", fileLabel, pageNumber,
                                    totalPagesToCompare, pagePassed ? "PASS" : "FAIL");
                            BufferedImage comparisonPage = createComparisonPage(baseImage, actualImage, diffCoordinates, header);
                            try {
                                synchronized (reportDocument) {
                                    addImagePageToReport(reportDocument, comparisonPage);
                                }
                            } finally {
                                comparisonPage.flush();
                            }

                            if (!pagePassed) {
                                allPagesPassed.set(false);
                            }
                        } finally {
                            baseTemp.delete();
                            actualTemp.delete();
                        }
                    } finally {
                        baseImage.flush();
                        actualImage.flush();
                    }
                    return null;
                };
                pageFutures.add(pageExecutor.submit(pageTask));
            }

            for (Future<Void> future : pageFutures) {
                try {
                    future.get();
                } catch (ExecutionException ee) {
                    logger.info(String.format("File %d/%d (%s) - a page comparison failed : %s", fileIndex,
                            totalFiles, fileLabel, ExceptionUtils.getStackTrace(ee.getCause())));
                    allPagesPassed.set(false);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    allPagesPassed.set(false);
                }
            }

            if (pageCountMismatch) {
                failureDetails.append(String.format("%s - page count mismatch (base=%d, actual=%d); ",
                        fileLabel, basePageCount, actualPageCount));
                addMessagePageToReport(reportDocument, String.format("%s - FAILED (page count mismatch: base has" +
                        " %d pages, actual has %d pages)", fileLabel, basePageCount, actualPageCount));
            } else if (!allPagesPassed.get()) {
                failureDetails.append(fileLabel).append(" - visual differences found; ");
            }
            return allPagesPassed.get() && !pageCountMismatch;
        } catch (OutOfMemoryError oom) {
            logger.info(String.format("OutOfMemoryError while comparing file %d/%d (%s) at page %d/%d : %s",
                    fileIndex, totalFiles, fileLabel, currentPage, pagesToCompare, ExceptionUtils.getStackTrace(oom)));
            String reason = currentPage > 0
                    ? String.format("ran out of memory (OutOfMemoryError) while rendering/comparing page %d of %d",
                            currentPage, pagesToCompare)
                    : "ran out of memory (OutOfMemoryError) before comparison could start";
            failureDetails.append(fileLabel).append(" - ").append(reason)
                    .append(" - try increasing the JVM heap size (-Xmx) for the agent or lowering the render DPI; ");
            try {
                addMessagePageToReport(reportDocument, fileLabel + " - FAILED (" + reason + ")");
            } catch (IOException | OutOfMemoryError innerError) {
                logger.info("Exception while adding out-of-memory failure page to report : " + ExceptionUtils.getStackTrace(innerError));
            }
            return false;
        } catch (Exception e) {
            logger.info("Exception while comparing file " + fileLabel + " : " + ExceptionUtils.getStackTrace(e));
            failureDetails.append(fileLabel).append(" - error during comparison: ").append(e.getMessage()).append("; ");
            try {
                addMessagePageToReport(reportDocument, fileLabel + " - FAILED (error during comparison: " + e.getMessage() + ")");
            } catch (IOException ioException) {
                logger.info("Exception while adding failure page to report : " + ExceptionUtils.getStackTrace(ioException));
            }
            return false;
        }
    }

    private BufferedImage createMessagePage(String message) {
        int width = 1000;
        int height = 150;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2d.drawString(message, 20, height / 2);
        g2d.dispose();
        return image;
    }

    private BufferedImage createComparisonPage(BufferedImage baseImage, BufferedImage actualImage,
                                               List<Coordinate> diffCoordinates, String header) {
        int headerHeight = 40;
        int width = baseImage.getWidth() + actualImage.getWidth();
        int height = Math.max(baseImage.getHeight(), actualImage.getHeight()) + headerHeight;
        BufferedImage page = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = page.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString(header, 10, 28);

        g2d.drawImage(baseImage, 0, headerHeight, null);
        g2d.drawImage(actualImage, baseImage.getWidth(), headerHeight, null);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g2d.setColor(new Color(255, 0, 0));
        for (Coordinate coordinate : diffCoordinates) {
            g2d.fillRect(coordinate.getX(), coordinate.getY() + headerHeight, coordinate.getW(), coordinate.getH());
            g2d.fillRect(coordinate.getX() + baseImage.getWidth(), coordinate.getY() + headerHeight,
                    coordinate.getW(), coordinate.getH());
        }
        g2d.dispose();
        return page;
    }

    private void addMessagePageToReport(PDDocument reportDocument, String message) throws IOException {
        BufferedImage image = createMessagePage(message);
        try {
            addImagePageToReport(reportDocument, image);
        } finally {
            image.flush();
        }
    }

    private void addImagePageToReport(PDDocument reportDocument, BufferedImage image) throws IOException {
        PDPage pdPage = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
        reportDocument.addPage(pdPage);
        PDImageXObject pdImage = LosslessFactory.createFromImage(reportDocument, image);
        try (PDPageContentStream contentStream = new PDPageContentStream(reportDocument, pdPage)) {
            contentStream.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());
        }
    }

    public ResponseObject performApiCall(File baseImage, File actualImage, int page) {
        try {
            logger.info(String.format("Performing visual testing for %s and %s", baseImage.getAbsolutePath(),
                    actualImage.getAbsolutePath()));
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("action", "COMPARE")
                    .addFormDataPart("scalingType", "BASE_IMAGE_SCALING")
                    .addFormDataPart(
                            "baseImageFile",
                            baseImage.getName(),
                            RequestBody.create(MediaType.parse("image/png"), baseImage)
                    )
                    .addFormDataPart(
                            "actualImageFile",
                            actualImage.getName(),
                            RequestBody.create(MediaType.parse("image/png"), actualImage)
                    );
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .url(Constants.VISUAL_SERVER_API_END_POINT)
                    .method("POST", requestBody)
                    .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        return mapper.readValue(responseBody, ResponseObject.class);
                    } else {
                        throw new RuntimeException(String.format("Visual testing failed at page %s, no response" +
                                " body present in the visual test response", page));
                    }
                } else {
                    throw new RuntimeException(String.format("Visual testing failed at page %s, error occurred" +
                            " internally", page));
                }
            }
        } catch (IOException e) {
            logger.info(String.format("Exception occurred while performing visual test at page %s : %s", page,
                    ExceptionUtils.getStackTrace(e)));
            throw new RuntimeException("Error occurred while performing visual test at page " + page, e);
        }
    }

}
