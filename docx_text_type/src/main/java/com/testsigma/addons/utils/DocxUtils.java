package com.testsigma.addons.utils;

import com.testsigma.sdk.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DocxUtils {
    WebDriver driver;
    Logger logger;

    public DocxUtils(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    public File urlToFileConverter(String url) {
        try {
            logger.info("url " + url);
            if (url.startsWith("https://") || url.startsWith("http://")) {
                String fileName = System.currentTimeMillis() + ".docx";
                URL urlObject = new URL(url);
                File tempFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created: " + tempFile.getName());
                return tempFile;
            }
            return new File(url);
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            logger.debug(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Unable to access the given docx: " + ExceptionUtils.getMessage(e));
        }
    }

    public boolean validateContentTypeForSearchText(String searchText, String filePath, String... formats) {
        File docxFile = urlToFileConverter(filePath);

        // 1. Search standard OOXML content (paragraphs, tables, headers, footers)
        try (FileInputStream fis = new FileInputStream(docxFile);
             XWPFDocument document = new XWPFDocument(fis)) {
            if (searchOoxml(document, searchText, formats)) return true;
        } catch (IOException e) {
            logger.info("Error while accessing: " + filePath);
            throw new RuntimeException("Error loading DOCX document: " + ExceptionUtils.getStackTrace(e));
        }

        // 2. Also search embedded altChunk (MHT/HTML) content if present
        return searchAltChunk(docxFile, searchText, formats);
    }

    private boolean searchOoxml(XWPFDocument document, String searchText, String[] formats) {
        if (searchInParagraphs(document.getParagraphs(), searchText, formats)) return true;
        for (XWPFTable table : document.getTables())
            if (searchTableRecursively(table, searchText, formats)) return true;
        for (XWPFHeader header : document.getHeaderList())
            if (searchInParagraphs(header.getParagraphs(), searchText, formats)) return true;
        for (XWPFFooter footer : document.getFooterList())
            if (searchInParagraphs(footer.getParagraphs(), searchText, formats)) return true;
        return false;
    }

    private boolean searchTableRecursively(XWPFTable table, String searchText, String[] formats) {
        for (XWPFTableRow row : table.getRows())
            for (XWPFTableCell cell : row.getTableCells()) {
                if (searchInParagraphs(cell.getParagraphs(), searchText, formats)) return true;
                for (XWPFTable nested : cell.getTables())
                    if (searchTableRecursively(nested, searchText, formats)) return true;
            }
        return false;
    }

    private boolean searchInParagraphs(List<XWPFParagraph> paragraphs, String searchText, String[] formats) {
        for (XWPFParagraph para : paragraphs) {
            String text = para.getText();
            if (text != null && text.contains(searchText) && checkAllFormatsInParagraph(para, searchText, formats))
                return true;
        }
        return false;
    }

    private boolean checkAllFormatsInParagraph(XWPFParagraph para, String searchText, String[] formats) {
        List<XWPFRun> runs = para.getRuns();
        StringBuilder full = new StringBuilder();
        List<Integer> charRunMap = new ArrayList<>();

        for (int i = 0; i < runs.size(); i++) {
            String rt = runs.get(i).getText(0);
            if (rt != null) {
                for (int c = 0; c < rt.length(); c++) charRunMap.add(i);
                full.append(rt);
            }
        }

        int idx = full.toString().indexOf(searchText);
        if (idx < 0) return false;

        Set<Integer> runIndices = new HashSet<>();
        for (int pos = idx; pos < idx + searchText.length(); pos++) runIndices.add(charRunMap.get(pos));

        for (int ri : runIndices) {
            XWPFRun run = runs.get(ri);
            String rt = run.getText(0);
            if (rt != null && rt.trim().isEmpty()) continue; // skip bare-whitespace runs
            for (String fmt : formats) {
                if (!ooxmlHasFormat(run, fmt)) return false;
            }
        }
        return true;
    }

    private boolean ooxmlHasFormat(XWPFRun run, String format) {
        switch (format.toLowerCase()) {
            case "bold":      return run.isBold();
            case "italic":    return run.isItalic();
            case "underline": return run.getUnderline() != null && run.getUnderline() != UnderlinePatterns.NONE;
            default:          return false;
        }
    }

    private boolean searchAltChunk(File docxFile, String searchText, String[] formats) {
        try (ZipFile zip = new ZipFile(docxFile)) {
            String altChunkPath = resolveAltChunkPath(zip);
            if (altChunkPath == null) return false;

            ZipEntry entry = zip.getEntry(altChunkPath);
            if (entry == null) {
                logger.info("altChunk entry not found in ZIP: " + altChunkPath);
                return false;
            }
            logger.info("Reading altChunk: " + altChunkPath);
            try (InputStream is = zip.getInputStream(entry)) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                // MHT starts with "MIME-Version:"; plain HTML does not
                String html = content.startsWith("MIME-Version:") ? extractHtmlFromMht(content) : content;
                return searchInHtml(Jsoup.parse(html), searchText, formats);
            }
        } catch (Exception e) {
            logger.info("Error reading altChunk: " + e.getMessage());
            return false;
        }
    }
    
    private String resolveAltChunkPath(ZipFile zip) throws Exception {
        ZipEntry relsEntry = zip.getEntry("word/_rels/document.xml.rels");
        if (relsEntry == null) return null;
        try (InputStream is = zip.getInputStream(relsEntry)) {
            String relsXml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Matcher m = Pattern.compile(
                "Type=\"[^\"]*aFChunk\"[^>]*Target=\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE).matcher(relsXml);
            if (!m.find()) return null;
            String target = m.group(1);
            return target.startsWith("/") ? target.substring(1) : "word/" + target;
        }
    }

    private boolean searchInHtml(Document doc, String searchText, String[] formats) {
        Set<String> required = new HashSet<>(Arrays.asList(formats));

        // Prefer elements that own the text directly; fall back to subtree match
        Elements candidates = doc.select("*:containsOwn(" + searchText + ")");
        if (candidates.isEmpty()) candidates = doc.select("*:contains(" + searchText + ")");

        for (Element el : candidates) {
            if (collectHtmlFormats(el).containsAll(required)) {
                logger.info("Found [" + searchText + "] with formats " + required);
                return true;
            }
        }
        logger.info("[" + searchText + "] not found with all required formats: " + required);
        return false;
    }

    private Set<String> collectHtmlFormats(Element el) {
        Set<String> formats = new HashSet<>();
        for (Element node = el; node != null; node = node.parent()) {
            switch (node.tagName().toLowerCase()) {
                case "u":                formats.add("underline"); break;
                case "b": case "strong": formats.add("bold");      break;
                case "i": case "em":     formats.add("italic");    break;
            }
            String style = node.attr("style");
            if (!style.isEmpty()) {
                if (style.contains("text-decoration") && style.contains("underline")) formats.add("underline");
                if (style.contains("font-style")      && style.contains("italic"))    formats.add("italic");
                if (style.contains("font-weight")) {
                    String wt = style.replaceAll(".*font-weight\\s*:\\s*([^;]+).*", "$1").trim();
                    if (wt.equals("bold") || wt.equals("bolder")) {
                        formats.add("bold");
                    } else {
                        try { if (Integer.parseInt(wt) >= 600) formats.add("bold"); }
                        catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return formats;
    }

    private String extractHtmlFromMht(String mhtContent) {
        String[] lines = mhtContent.split("\r?\n");
        boolean inHtmlPart = false, pastHeaders = false;
        StringBuilder qp = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("------=mhtDocumentPart") || line.startsWith("--")) {
                if (inHtmlPart && qp.length() > 0) break;
                inHtmlPart = false; pastHeaders = false; qp.setLength(0);
                continue;
            }
            if (!pastHeaders) {
                if (line.isBlank()) { pastHeaders = true; continue; }
                if (line.toLowerCase().startsWith("content-type:") && line.toLowerCase().contains("text/html"))
                    inHtmlPart = true;
                continue;
            }
            if (inHtmlPart) qp.append(line).append("\r\n");
        }
        return decodeQuotedPrintable(qp.toString());
    }

    private String decodeQuotedPrintable(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); ) {
            char c = input.charAt(i);
            if (c == '=' && i + 1 < input.length()) {
                char n = input.charAt(i + 1);
                if (n == '\r' || n == '\n') {
                    i += (n == '\r' && i + 2 < input.length() && input.charAt(i + 2) == '\n') ? 3 : 2;
                } else if (i + 2 < input.length() && isHex(n) && isHex(input.charAt(i + 2))) {
                    sb.append((char) Integer.parseInt(input.substring(i + 1, i + 3), 16));
                    i += 3;
                } else { sb.append(c); i++; }
            } else { sb.append(c); i++; }
        }
        return sb.toString();
    }

    private boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
