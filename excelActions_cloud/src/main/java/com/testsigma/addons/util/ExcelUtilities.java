package com.testsigma.addons.util;

import java.io.File;

public interface ExcelUtilities {
    File copyFileFromDownloads(String fileFormat, String fileName) throws Exception;
    boolean isFileDownloaded();
    String getDownloadedFileLocalPath();
    String getFilePathByFileNameInDownloads(String desiredFileName);
}