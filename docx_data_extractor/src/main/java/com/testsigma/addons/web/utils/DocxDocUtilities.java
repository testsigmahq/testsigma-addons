package com.testsigma.addons.web.utils;

import java.io.File;

public interface DocxDocUtilities {
    File copyFileFromDownloads(String fileFormat, String fileName) throws Exception;
    boolean isFileDownloaded();
    String getDownloadedFileLocalPath();
    String getFilePathByFileNameInDownloads(String desiredFileName);
}