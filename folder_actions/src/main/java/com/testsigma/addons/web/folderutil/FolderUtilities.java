package com.testsigma.addons.web.folderutil;

import java.io.File;

public class FolderUtilities {

    public String FOLDER_NOT_FOUND = "There is no folder exists in the given path %s";
    public String NO_FILE_EXISTS_ERROR_MSG = "There is no file exists with name %s in the folder path %s";

    public String NO_FILE_CONTAINS_ERROR_MSG = "There is no file exists with containing the name %s in the folder path %s";

    public File searchFile(File directory, String fileName, boolean isEqualOperation) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        File foundFile = searchFile(file, fileName, isEqualOperation);
                        if (foundFile != null) {
                            return foundFile;
                        }
                    } else {
                        if (isEqualOperation && file.getName().equals(fileName)) {
                            return file;
                        } else if (!isEqualOperation && file.getName().contains(fileName)) {
                            return file;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean folderCheck(String folderPath) {
        File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory();
    }
}
