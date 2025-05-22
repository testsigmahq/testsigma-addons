package com.testsigma.addons.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LoggerService {

  public void printLogs(String logMessage) {
    try {
      File file = new File(GlobalStateService.getInstance().getLogFilePath());
      try (FileWriter fileWriter = new FileWriter(file, true)) {
        fileWriter.append(logMessage).append("\n");
      }
    } catch (IOException ignored) {
    }
  }

}
