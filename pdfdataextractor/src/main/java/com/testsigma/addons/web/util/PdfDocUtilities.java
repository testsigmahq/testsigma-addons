package com.testsigma.addons.web.util;

import java.io.File;

public interface PdfDocUtilities {
    File copyFileFromDownloads(String fileFormat, String fileName) throws Exception;
    boolean isFileDownloaded();
    String getDownloadedFileLocalPath();
    String getFilePathByFileNameInDownloads(String desiredFileName);
}