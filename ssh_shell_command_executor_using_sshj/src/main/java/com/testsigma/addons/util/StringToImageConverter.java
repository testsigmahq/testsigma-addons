package com.testsigma.addons.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a string into an image with white background and the text drawn on it.
 * Font size is chosen based on the input text length (shorter text = larger font).
 */
public final class StringToImageConverter {

    private static final int MIN_FONT_SIZE = 12;
    private static final int MAX_FONT_SIZE = 72;
    private static final int PADDING = 40;
    private static final int MIN_IMAGE_WIDTH = 200;
    private static final int MAX_IMAGE_WIDTH = 1200;
    private static final String FONT_NAME = Font.SANS_SERIF;

    private StringToImageConverter() {
    }

    /**
     * Converts the given text into a BufferedImage with white background and black text.
     * Font size is derived from text length.
     *
     * @param text the string to render (can be null or empty; empty yields a small placeholder image)
     * @return BufferedImage with white background and text
     */
    public static BufferedImage convert(String text) {
        String safeText = text == null ? "" : text;
        int fontSize = computeFontSize(safeText.length());
        Font font = new Font(FONT_NAME, Font.PLAIN, fontSize);

        FontMetrics fm = getFontMetrics(font);
        List<String> lines = wrapLines(safeText, fm, MAX_IMAGE_WIDTH - 2 * PADDING);
        int lineHeight = fm.getHeight();
        int ascent = fm.getAscent();
        int imageWidth = Math.max(MIN_IMAGE_WIDTH, computeTextWidth(lines, fm) + 2 * PADDING);
        int imageHeight = Math.max(40, lines.size() * lineHeight + 2 * PADDING);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, imageWidth, imageHeight);
            g.setColor(Color.BLACK);
            g.setFont(font);

            int y = PADDING + ascent;
            for (String line : lines) {
                g.drawString(line, PADDING, y);
                y += lineHeight;
            }
        } finally {
            g.dispose();
        }
        return image;
    }

    /**
     * Converts the given text to an image and writes it to a temporary PNG file.
     *
     * @param text the string to render (can be CharSequence e.g. StringBuilder to avoid extra copy)
     * @return temporary File containing the PNG image
     * @throws IOException if writing the file fails
     */
    public static File convertToFile(CharSequence text) throws IOException {
        BufferedImage image = convert(text == null ? null : text.toString());
        File tempFile = File.createTempFile("testsigma-text-image-", ".png");
        ImageIO.write(image, "png", tempFile);
        return tempFile;
    }

    private static int computeFontSize(int textLength) {
        if (textLength <= 0) return MAX_FONT_SIZE;
        if (textLength <= 15) return MAX_FONT_SIZE;
        if (textLength <= 50) return 48;
        if (textLength <= 150) return 32;
        if (textLength <= 400) return 24;
        if (textLength <= 800) return 18;
        return Math.max(MIN_FONT_SIZE, 14);
    }

    private static FontMetrics getFontMetrics(Font font) {
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dummy.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        g.dispose();
        return fm;
    }

    private static List<String> wrapLines(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) {
            lines.add(" ");
            return lines;
        }
        String[] paragraphs = text.split("\\n", -1);
        for (String para : paragraphs) {
            if (para.isEmpty()) {
                lines.add(" ");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : para.split(" ", -1)) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(candidate) <= maxWidth) {
                    line.setLength(0);
                    line.append(candidate);
                } else {
                    if (line.length() > 0) {
                        lines.add(line.toString());
                        line.setLength(0);
                    }
                    if (fm.stringWidth(word) <= maxWidth) {
                        line.append(word);
                    } else {
                        for (int i = 0; i < word.length(); ) {
                            int fit = 0;
                            int maxFit = word.length() - i;
                            while (fit < maxFit && fm.stringWidth(word.substring(i, i + fit + 1)) <= maxWidth) {
                                fit++;
                            }
                            if (fit == 0) fit = 1;
                            lines.add(word.substring(i, i + fit));
                            i += fit;
                        }
                    }
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        return lines.isEmpty() ? List.of(" ") : lines;
    }

    private static int computeTextWidth(List<String> lines, FontMetrics fm) {
        int max = 0;
        for (String line : lines) {
            max = Math.max(max, fm.stringWidth(line));
        }
        return max;
    }
}
