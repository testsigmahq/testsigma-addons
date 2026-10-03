package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

public class ImageMatchUtils {

    private static final int MAX_SCALES = 30;
    private static final int MIN_TEMPLATE_DIM = 8;

    public static class MatchResult {
        public final boolean found;
        public final int x1, y1, x2, y2;
        public final double confidence;
        public final String method;
        public final String message;

        public MatchResult(boolean found, int x1, int y1, int x2, int y2,
                           double confidence, String method, String message) {
            this.found = found;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.confidence = confidence;
            this.method = method;
            this.message = message;
        }

        public static MatchResult notFound(String method, String msg) {
            return new MatchResult(false, 0, 0, 0, 0, 0, method, msg);
        }
    }

    // ======================== Multi-Scale Search ========================

    public static MatchResult searchMultiScale(double[][] baseData, int bw, int bh,
                                               double[][] tmplGray, int tw, int th,
                                               boolean edgeMode, double threshold,
                                               String methodName, Logger logger) {
        int tmplMaxDim = Math.max(tw, th);
        double minScale = Math.max(0.05, (double) MIN_TEMPLATE_DIM / tmplMaxDim);
        double maxScale = Math.min(4.0,
                (double) Math.min(bw, bh) * 0.5 / Math.max(tw, th));

        if (minScale >= maxScale) {
            return MatchResult.notFound(methodName,
                    "Invalid scale range [" + minScale + ", " + maxScale + "]");
        }

        int numScales = Math.min(MAX_SCALES,
                Math.max(10, (int) ((maxScale - minScale) / 0.03) + 1));
        logger.info(String.format("%s: scale %.3f->%.3f, %d steps, edgeMode=%b",
                methodName, minScale, maxScale, numScales, edgeMode));

        double bestScore = -1;
        int bestX = 0, bestY = 0, bestW = 0, bestH = 0;

        for (int si = 0; si < numScales; si++) {
            double scale = maxScale - (maxScale - minScale) * si / Math.max(1, numScales - 1);
            int nw = (int) Math.round(tw * scale);
            int nh = (int) Math.round(th * scale);

            if (nw < MIN_TEMPLATE_DIM || nh < MIN_TEMPLATE_DIM || nw >= bw || nh >= bh) continue;

            double[][] scaledTmpl = resize(tmplGray, tw, th, nw, nh);
            double[][] searchTmpl = edgeMode
                    ? sobelEdgeDetection(scaledTmpl, nw, nh)
                    : scaledTmpl;

            double tmplMean = mean(searchTmpl, nw, nh);
            double tmplStd = std(searchTmpl, nw, nh, tmplMean);
            if (tmplStd < 1e-6) continue;

            int step = Math.max(1, Math.min(nw, nh) / 4);

            double scaleBest = -1;
            int sBestX = 0, sBestY = 0;

            for (int y = 0; y <= bh - nh; y += step) {
                for (int x = 0; x <= bw - nw; x += step) {
                    double ncc = computeNCC(baseData, bw, searchTmpl, nw, nh,
                            x, y, tmplMean, tmplStd);
                    if (ncc > scaleBest) {
                        scaleBest = ncc;
                        sBestX = x;
                        sBestY = y;
                    }
                }
            }

            if (step > 1 && scaleBest > threshold * 0.6) {
                int rx = sBestX, ry = sBestY;
                for (int dy = -step; dy <= step; dy++) {
                    for (int dx = -step; dx <= step; dx++) {
                        int cx = rx + dx, cy = ry + dy;
                        if (cx < 0 || cy < 0 || cx + nw > bw || cy + nh > bh) continue;
                        double ncc = computeNCC(baseData, bw, searchTmpl, nw, nh,
                                cx, cy, tmplMean, tmplStd);
                        if (ncc > scaleBest) {
                            scaleBest = ncc;
                            sBestX = cx;
                            sBestY = cy;
                        }
                    }
                }
            }

            if (scaleBest > bestScore) {
                bestScore = scaleBest;
                bestX = sBestX;
                bestY = sBestY;
                bestW = nw;
                bestH = nh;
            }
        }

        logger.info(String.format("%s: best NCC=%.4f at (%d,%d) size %dx%d",
                methodName, bestScore, bestX, bestY, bestW, bestH));

        if (bestScore >= threshold) {
            return new MatchResult(true, bestX, bestY,
                    bestX + bestW, bestY + bestH, bestScore, methodName, "Matched");
        }
        return MatchResult.notFound(methodName,
                String.format("Best NCC %.4f < threshold %.4f", bestScore, threshold));
    }

    // ======================== NCC Computation ========================

    public static double computeNCC(double[][] base, int baseW,
                                    double[][] tmpl, int tw, int th,
                                    int sx, int sy,
                                    double tmplMean, double tmplStd) {
        int n = tw * th;
        double baseMean = 0;
        for (int y = 0; y < th; y++)
            for (int x = 0; x < tw; x++)
                baseMean += base[sy + y][sx + x];
        baseMean /= n;

        double crossCorr = 0;
        double baseVar = 0;
        for (int y = 0; y < th; y++) {
            for (int x = 0; x < tw; x++) {
                double bd = base[sy + y][sx + x] - baseMean;
                double td = tmpl[y][x] - tmplMean;
                crossCorr += bd * td;
                baseVar += bd * bd;
            }
        }

        double baseStd = Math.sqrt(baseVar / n);
        if (baseStd < 1e-6) return 0;
        return crossCorr / (n * baseStd * tmplStd);
    }

    // ======================== Image Processing ========================

    public static double[][] toGrayscale(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();

        BufferedImage opaque = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = opaque.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.drawImage(img, 0, 0, null);
        g.dispose();

        double[][] gray = new double[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = opaque.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int gv = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                gray[y][x] = 0.299 * r + 0.587 * gv + 0.114 * b;
            }
        }
        return gray;
    }

    public static double[][] resize(double[][] img, int ow, int oh, int nw, int nh) {
        double[][] out = new double[nh][nw];
        double xr = (double) ow / nw;
        double yr = (double) oh / nh;
        for (int y = 0; y < nh; y++) {
            double srcY = y * yr;
            int sy = Math.min((int) srcY, oh - 1);
            int sy1 = Math.min(sy + 1, oh - 1);
            double fy = srcY - sy;
            for (int x = 0; x < nw; x++) {
                double srcX = x * xr;
                int sx = Math.min((int) srcX, ow - 1);
                int sx1 = Math.min(sx + 1, ow - 1);
                double fx = srcX - sx;
                out[y][x] = (1 - fx) * (1 - fy) * img[sy][sx]
                        + fx * (1 - fy) * img[sy][sx1]
                        + (1 - fx) * fy * img[sy1][sx]
                        + fx * fy * img[sy1][sx1];
            }
        }
        return out;
    }

    public static double[][] sobelEdgeDetection(double[][] gray, int w, int h) {
        double[][] edges = new double[h][w];
        double maxVal = 0;
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                double gx = -gray[y - 1][x - 1] + gray[y - 1][x + 1]
                        - 2 * gray[y][x - 1] + 2 * gray[y][x + 1]
                        - gray[y + 1][x - 1] + gray[y + 1][x + 1];
                double gy = -gray[y - 1][x - 1] - 2 * gray[y - 1][x] - gray[y - 1][x + 1]
                        + gray[y + 1][x - 1] + 2 * gray[y + 1][x] + gray[y + 1][x + 1];
                double mag = Math.sqrt(gx * gx + gy * gy);
                edges[y][x] = mag;
                if (mag > maxVal) maxVal = mag;
            }
        }
        if (maxVal > 0) {
            double inv = 255.0 / maxVal;
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    edges[y][x] *= inv;
        }
        return edges;
    }

    public static double mean(double[][] data, int w, int h) {
        double sum = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                sum += data[y][x];
        return sum / (w * h);
    }

    public static double std(double[][] data, int w, int h, double mean) {
        double sumSq = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double d = data[y][x] - mean;
                sumSq += d * d;
            }
        return Math.sqrt(sumSq / (w * h));
    }

    // ======================== File Utilities ========================

    public static int[] getImageFileDimensions(File imageFile) throws Exception {
        try (ImageInputStream iis = ImageIO.createImageInputStream(imageFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis);
                    return new int[]{reader.getWidth(0), reader.getHeight(0)};
                } finally {
                    reader.dispose();
                }
            }
        }
        BufferedImage img = ImageIO.read(imageFile);
        return new int[]{img.getWidth(), img.getHeight()};
    }

    public static File downloadImage(String fileName, String url, Logger logger) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Downloading: " + url);
                URL urlObject = new URL(url);
                String extension = ".png";
                String urlPath = urlObject.getPath();
                int dot = urlPath.lastIndexOf('.');
                if (dot > 0) extension = urlPath.substring(dot);

                File tempFile = File.createTempFile(fileName, extension);
                try (InputStream in = urlObject.openStream()) {
                    Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                logger.info("Downloaded to: " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Local path: " + url);
                return new File(url);
            }
        } catch (Exception e) {
            logger.info("Image access error: " + url);
            throw new RuntimeException("Unable to access image: " + url, e);
        }
    }

    public static void cleanupFile(File file) {
        try {
            if (file != null && file.exists() && file.isFile()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }
}
