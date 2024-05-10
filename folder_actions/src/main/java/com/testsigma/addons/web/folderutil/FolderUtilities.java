package com.testsigma.addons.web.folderutil;

import com.testsigma.sdk.Logger;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

public class FolderUtilities {

    public String errorInfo;

    public String successInfo;

    public File fileExists(String folderPath, String fileName) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File file = new File(folder, fileName);
            if (file.exists() && file.isFile()) {
                successInfo = String.format("Successfully verified that file exists with name %s in the folder " +
                        "path %s", fileName, folderPath);
                return file;
            } else {
                errorInfo = String.format("There is no file exists with name %s in the folder path %s", fileName,
                        folderPath);
            }
        } else {
            errorInfo = String.format("There is no folder exists in the given path %s",folderPath);
        }
        return null;
    }

    public File fileContainsName(String folderPath, String fileName) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(fileName)) {
                        successInfo = String.format("Successfully verified that file exists with containing the name %s in " +
                                "the folder path %s", fileName, folderPath);
                        return file;
                    }
                }
            } else {
                errorInfo = String.format("There are no files present in the folder path %s", folderPath);
                return null;
            }
        } else {
            errorInfo = String.format("There is no folder exists in the given path %s",folderPath);
            return null;
        }
        errorInfo = String.format("There is no file exists with containing the name %s in the folder path %s",
                    fileName, folderPath);
        return null;
    }
}
