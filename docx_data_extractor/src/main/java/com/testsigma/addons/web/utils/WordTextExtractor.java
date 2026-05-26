package com.testsigma.addons.web.utils;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordTextExtractor {

    private static final String ALT_CHUNK_REL_TYPE =
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk";

    public static String extract(File file, boolean isDocx) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            if (isDocx) {
                try (XWPFDocument document = new XWPFDocument(fis)) {
                    return extractDocx(document);
                }
            } else {
                try (HWPFDocument document = new HWPFDocument(fis);
                     WordExtractor extractor = new WordExtractor(document)) {
                    return extractor.getText();
                }
            }
        }
    }

    private static String extractDocx(XWPFDocument document) throws Exception {
        // Standard XML paragraph walk (handles body, tables, text boxes, SDT)
        StringBuilder sb = new StringBuilder();
        XmlObject[] paras = document.getDocument().getBody().selectPath(
                "declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:p"
        );
        for (XmlObject para : paras) {
            XmlObject[] runs = para.selectPath(
                    "declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:t"
            );
            for (XmlObject run : runs) {
                sb.append(run.getDomNode().getTextContent());
            }
            sb.append("\n");
        }
        String text = sb.toString().trim();

        // Fallback: Teams/Outlook exports embed content as an MHT altChunk with no <w:t> elements
        if (text.isEmpty()) {
            text = extractAltChunk(document);
        }
        return text;
    }

    private static String extractAltChunk(XWPFDocument document) throws Exception {
        for (PackageRelationship rel : document.getPackagePart().getRelationships()) {
            if (!ALT_CHUNK_REL_TYPE.equals(rel.getRelationshipType())) continue;
            PackagePart chunkPart = document.getPackage().getPart(
                    PackagingURIHelper.createPartName(rel.getTargetURI()));
            if (chunkPart == null) continue;
            try (InputStream is = chunkPart.getInputStream()) {
                String mht = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                return stripHtml(decodeQuotedPrintable(extractHtmlFromMht(mht)));
            }
        }
        return "";
    }

    private static String extractHtmlFromMht(String mht) {
        Matcher bm = Pattern.compile("boundary=\"([^\"]+)\"").matcher(mht);
        if (!bm.find()) return mht;
        String boundary = bm.group(1);

        String[] parts = mht.split("--" + Pattern.quote(boundary));
        for (String part : parts) {
            if (part.toLowerCase().contains("content-type: text/html")) {
                int bodyStart = part.indexOf("\r\n\r\n");
                if (bodyStart == -1) bodyStart = part.indexOf("\n\n");
                if (bodyStart != -1) return part.substring(bodyStart).trim();
            }
        }
        return mht;
    }

    private static String decodeQuotedPrintable(String qp) throws Exception {
        qp = qp.replace("=\r\n", "").replace("=\n", "");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;
        while (i < qp.length()) {
            char c = qp.charAt(i);
            if (c == '=' && i + 2 < qp.length()) {
                String hex = qp.substring(i + 1, i + 3);
                try {
                    baos.write(Integer.parseInt(hex, 16));
                    i += 3;
                } catch (NumberFormatException e) {
                    baos.write((byte) c);
                    i++;
                }
            } else {
                baos.write((byte) c);
                i++;
            }
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    private static String stripHtml(String html) {
        html = html.replaceAll("(?is)<style[^>]*>.*?</style>", " ");
        html = html.replaceAll("(?is)<script[^>]*>.*?</script>", " ");
        html = html.replaceAll("(?i)<(br|/p|/div|/h[1-6]|/li|/tr)[^>]*>", "\n");
        html = html.replaceAll("<[^>]+>", "");
        html = html.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                   .replace("&nbsp;", " ").replace("&quot;", "\"").replace("&#39;", "'");
        html = html.replaceAll("[ \\t]+", " ");
        html = html.replaceAll("([ \\t]*\\n){3,}", "\n\n");
        return html.trim();
    }
}
