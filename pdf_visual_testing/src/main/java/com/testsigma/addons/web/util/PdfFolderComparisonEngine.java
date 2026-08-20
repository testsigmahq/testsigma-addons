package com.testsigma.addons.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.Logger;
import okhttp3.ConnectionPool;
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

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared implementation behind the "PDF Visual Testing For Folder" action, reused by both the
 * web and windowsAdvanced application-type copies (which otherwise differed only in package,
 * superclass, and applicationType). Pairs pdf files across two folders, compares them page by
 * page against the visual-testing API, and builds one consolidated report pdf.
 */
public class PdfFolderComparisonEngine {

    // Number of pages compared concurrently per file. The remote visual-comparison API call is
    // a network round trip and dominates page processing time; performing them one at a time
    // per page serializes hundreds/thousands of round trips. PDF rendering itself stays
    // single-threaded (PDFRenderer/PDDocument are not safe for concurrent use).
    private static final int PAGE_CONCURRENCY = 10;

    private static final ObjectMapper mapper = new ObjectMapper();

    // Shared, connection-pooled HTTP client reused across every page/file so pages benefit from
    // HTTP keep-alive instead of paying a fresh TCP+TLS handshake on every single page. Explicit
    // timeouts matter here: OkHttp's 10s connect/read/write defaults are too short for
    // multi-megabyte PNG uploads and would otherwise surface as false FAILs.
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES))
            .connectTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(120))
            .writeTimeout(Duration.ofSeconds(120))
            .callTimeout(Duration.ofSeconds(180))
            .build();

    private final Logger logger;

    public PdfFolderComparisonEngine(Logger logger) {
        this.logger = logger;
    }

    /** Result of comparing every paired pdf file across the two folders. */
    public static class Outcome {
        public final int filesPassed;
        public final int filesFailed;
        public final String failureDetails;
        public final String setupError;
        public final File reportFile;

        Outcome(int filesPassed, int filesFailed, String failureDetails, String setupError, File reportFile) {
            this.filesPassed = filesPassed;
            this.filesFailed = filesFailed;
            this.failureDetails = failureDetails;
            this.setupError = setupError;
            this.reportFile = reportFile;
        }
    }

    /** Thrown only when the consolidated report pdf itself could not be built/saved. */
    public static class ReportGenerationException extends RuntimeException {
        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static String buildReportLink(File reportFile) {
        String url = reportFile.toURI().toString();
        return String.format("<a href=\"%s\" target=\"_blank\" rel=\"noopener noreferrer\">%s</a>",
                url, reportFile.getAbsolutePath());
    }

    public Outcome compareFolders(String baseFolderPath, String actualFolderPath, String pairingStrategy) {
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
            pageExecutor.shutdownNow();
            throw new ReportGenerationException("Unable to generate the consolidated visual testing report pdf" +
                    " because the JVM ran out of memory (OutOfMemoryError). Try increasing the JVM heap size" +
                    " (-Xmx) for the agent or lowering the render DPI.", oom);
        } catch (Exception e) {
            logger.info("Exception while building the consolidated report pdf : " + ExceptionUtils.getStackTrace(e));
            pageExecutor.shutdownNow();
            throw new ReportGenerationException("Unable to generate the consolidated visual testing report pdf : "
                    + e.getMessage(), e);
        } finally {
            pageExecutor.shutdown();
            try {
                pageExecutor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        return new Outcome(filesPassed, filesFailed, failureDetails.toString(), setupError, reportFile);
    }

    private Map<File, File> pairFiles(File baseFolder, File actualFolder, String pairingStrategy,
                                       List<String> unmatchedBaseFiles) {
        File[] baseFilesArr = baseFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        File[] actualFilesArr = actualFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        List<File> baseFiles = new ArrayList<>(baseFilesArr == null ? Collections.emptyList() : java.util.Arrays.asList(baseFilesArr));
        List<File> actualFiles = new ArrayList<>(actualFilesArr == null ? Collections.emptyList() : java.util.Arrays.asList(actualFilesArr));
        baseFiles.sort(Comparator.comparing(File::getName));
        actualFiles.sort(Comparator.comparing(File::getName));

        logger.info(String.format("Found %d pdf file(s) in base folder and %d pdf file(s) in actual folder," +
                " pairing by '%s'", baseFiles.size(), actualFiles.size(), pairingStrategy));

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
        // Each page's rendered comparison image is written to its own temp PNG, keyed by page
        // number, instead of being appended to reportDocument from the completing task. Pages
        // are appended to the report in page order, on this single thread, only after every
        // page task has been awaited below - so reportDocument is never mutated from more than
        // one thread, and the report always reads in document order regardless of which page's
        // API call happens to finish first.
        ConcurrentSkipListMap<Integer, File> orderedPageImages = new ConcurrentSkipListMap<>();
        AtomicBoolean allPagesPassed = new AtomicBoolean(true);
        List<Future<Void>> pageFutures = new ArrayList<>();
        try (PDDocument baseDocument = Loader.loadPDF(basePdf);
             PDDocument actualDocument = Loader.loadPDF(actualPdf)) {
            int basePageCount = baseDocument.getNumberOfPages();
            int actualPageCount = actualDocument.getNumberOfPages();
            pagesToCompare = Math.min(basePageCount, actualPageCount);
            int totalPagesToCompare = pagesToCompare;
            boolean pageCountMismatch = basePageCount != actualPageCount;

            logger.info(String.format("File %d/%d (%s) - %d page(s) to compare (base has %d page(s), actual has" +
                    " %d page(s))", fileIndex, totalFiles, fileLabel, pagesToCompare, basePageCount, actualPageCount));

            PDFRenderer baseRenderer = new PDFRenderer(baseDocument);
            PDFRenderer actualRenderer = new PDFRenderer(actualDocument);

            try {
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
                                File pageTemp = File.createTempFile("report_page_", ".png");
                                try {
                                    ImageIO.write(comparisonPage, "png", pageTemp);
                                    orderedPageImages.put(pageNumber, pageTemp);
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
            } finally {
                // Always await whatever was submitted so far - including when renderImageWithDPI
                // throws partway through the loop - so that by the time control reaches the
                // catch blocks below (or the report-appending step further down), no page task
                // is still running.
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
            }

            appendOrderedPagesToReport(reportDocument, orderedPageImages);

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
            deleteOrderedPages(orderedPageImages);
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
            deleteOrderedPages(orderedPageImages);
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

    private void appendOrderedPagesToReport(PDDocument reportDocument, ConcurrentSkipListMap<Integer, File> orderedPageImages)
            throws IOException {
        for (File pageTemp : orderedPageImages.values()) {
            try {
                BufferedImage pageImage = ImageIO.read(pageTemp);
                try {
                    addImagePageToReport(reportDocument, pageImage);
                } finally {
                    pageImage.flush();
                }
            } finally {
                pageTemp.delete();
            }
        }
        orderedPageImages.clear();
    }

    private void deleteOrderedPages(ConcurrentSkipListMap<Integer, File> orderedPageImages) {
        for (File pageTemp : orderedPageImages.values()) {
            pageTemp.delete();
        }
        orderedPageImages.clear();
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

    private ResponseObject performApiCall(File baseImage, File actualImage, int page) {
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
