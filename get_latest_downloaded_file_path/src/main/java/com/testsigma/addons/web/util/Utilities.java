package com.testsigma.addons.web.util;

import java.io.File;

public interface Utilities {

    File copyFileFromDownloads() throws Exception;

    boolean isFileDownloaded();

    String getDownloadedFileLocalPath();
}