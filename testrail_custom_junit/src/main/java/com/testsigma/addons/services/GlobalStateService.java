package com.testsigma.addons.services;

import lombok.Data;

@Data
public class GlobalStateService {

  private static GlobalStateService _instance = null;

  public String workingDirectory;
  public String logFilePath;
  public String junitFilePath;
  public String javaPath;

  private GlobalStateService() {}

  public static GlobalStateService getInstance() {
    if (_instance == null) {
      _instance = new GlobalStateService();
    }
    return _instance;
  }

  public static void clear() {
    _instance = null;
  }
}
