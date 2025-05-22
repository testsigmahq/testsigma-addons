package com.testsigma.addons.hooks;


import com.testsigma.addons.services.*;
import com.testsigma.sdk.ExecutionHierarchy;
import com.testsigma.sdk.Hook;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.*;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@TestPlanHook(name = "Testrail Addon hook")
public class TestRailAddon extends Hook {

  @JunitReport
  private static com.testsigma.sdk.JunitReport junitReport;

  @TestRailCredentials
  private com.testsigma.sdk.TestRailCredentials testRailCredentials;

  @CICDCredentials
  private com.testsigma.sdk.runners.CICDCredentials cicdCredentials;

  @TestData(reference = "{PROJECT_NAME}", description = "TestRail project name (required)")
  private com.testsigma.sdk.TestData projectName;

  @TestData(reference = "{API_KEY}", description = "Testsigma API key (required)")
  private com.testsigma.sdk.TestData testSigmaAPIKey;

  @TestData(reference = "{TEMPLATE_URL}", description = "Custom Junit template URL (optional)")
  private com.testsigma.sdk.TestData templateURL;

  @RunResult
  private com.testsigma.sdk.RunResult runResult;

  @RunTimeDataProvider
  private com.testsigma.sdk.RuntimeDataProvider runTimeDataProvider;

  private final ZipService zipService = new ZipService();
  private final UploadToStorageService uploadToStorageService = new UploadToStorageService();
  private final LoggerService loggerService = new LoggerService();
  private final TestrailAPIClientService testrailAPIClientService = new TestrailAPIClientService();
  private final CLIDownloaderService clidownloaderService = new CLIDownloaderService();
  private final CustomJunitReportService customJunitReportService = new CustomJunitReportService();
  private final JunitReportResultHandlerService junitReportResultHandlerService = new JunitReportResultHandlerService();

  @Override
  protected Result execute() {
    try {
      initStates();
      loggerService.printLogs("Starting testrail result upload addon execution");

      // print test data
      loggerService.printLogs("Default junit report: " + junitReport.getJunitReport());
      loggerService.printLogs("projectName: " + projectName.getValue());
      loggerService.printLogs("templateURL: " + templateURL.getValue());
      if (System.getProperty("TS_ROOT_DIR") != null) {
        loggerService.printLogs("TS_ROOT_DIR: " + System.getProperty("TS_ROOT_DIR"));
      }
      if (System.getProperty("TS_DATA_DIR") != null) {
        loggerService.printLogs("TS_DATA_DIR: " + System.getProperty("TS_DATA_DIR"));
      }


      // generate report
      if (templateURL != null && templateURL.getValue() != null) {
        loggerService.printLogs("Generating custom junit report " + templateURL.getValue());
        String templateUrl = templateURL.getValue().toString();
        String apiKey = testSigmaAPIKey.getValue().toString();
        customJunitReportService.generateCustomJunit(templateUrl, apiKey, runResult.getId());
        loggerService.printLogs("Custom junit report generated successfully");
      } else {
        loggerService.printLogs("Using default Junit report");
        writeJunitReportToFile();
      }

      // mark stopped results to blocked
      junitReportResultHandlerService.markStoppedResultAsBlocked();

      // download the executable from S3
      String pathToExe = clidownloaderService.downloadExe();

      // creating the trcli command
      List<String> cmd = createTRCLICommand(pathToExe);
      loggerService.printLogs("TRCLI Command : " + Arrays.toString(cmd.toArray()));

      // run the command
      Process process = executeTrcliCommand(cmd);

      // check if command run was successful or not
      int exitCode = 0;
      try {
        exitCode = process.waitFor();
      } catch (InterruptedException e) {
        loggerService.printLogs("Command Run failed with error : " + e.getMessage());
      }

      // return Addon Execution Status
      if (exitCode == 0) {
        return Result.SUCCESS;
      } else {
        return Result.FAILED;
      }
    } catch (Exception e) {
      loggerService.printLogs(" Error while executing Addon : " + e.getMessage());
      return Result.FAILED;
    } finally {
      createAndUploadAddonOutputZip();
      // try {
      // cleanUp();
      // } catch (IOException ignored) {
      // }
    }
  }

  private void initStates() {
    GlobalStateService stateService = GlobalStateService.getInstance();

    // arguments
    String randomUUID = UUID.randomUUID().toString();
    String rootPath = System.getProperty("TS_ROOT_DIR");
    String testsigmaDataPath = System.getProperty("TS_DATA_DIR");

    // set working directory and log file path
    String workingDirectory = testsigmaDataPath + "/testrail-output/run" + runResult.getId() + "-" + randomUUID;
    stateService.setWorkingDirectory(workingDirectory);
    stateService.setLogFilePath(workingDirectory + File.separator + "logs.log");
    stateService.setJunitFilePath(workingDirectory + File.separator + "junit_report.xml");
    createFiles();

    // set java path
    String javaPath = rootPath + File.separator + "jre" + "/bin/java";
    stateService.setJavaPath(javaPath);
  }

  @SneakyThrows
  private void createFiles() {
    GlobalStateService stateService = GlobalStateService.getInstance();

    // create working directory
    File file = new File(stateService.getWorkingDirectory());
    if (!file.exists()) {
      boolean ignored = file.mkdirs();
    }

    // create log file
    file = new File(stateService.getLogFilePath());
    if (!file.exists()) {
      boolean ignored = file.createNewFile();
    }
  }

  private Process executeTrcliCommand(List<String> cmd) {
    ProcessBuilder processBuilder = new ProcessBuilder(cmd);
    Process process = null;
    try {
      process = processBuilder.start();
    } catch (IOException e) {
      loggerService.printLogs("Exception while running process builder : " + e.getMessage());
    }

    // Read standard output
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    } catch (IOException e) {
      loggerService.printLogs("Exception while reading output: " + e.getMessage());
    }
    loggerService.printLogs("Output : " + output);

    // Read error output
    StringBuilder errorOutput = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        errorOutput.append(line).append("\n");
      }
    } catch (IOException e) {
      loggerService.printLogs("Exception while reading error output : " + e.getMessage());
    }
    loggerService.printLogs("Error Output : " + errorOutput);
    return process;
  }

  private void writeJunitReportToFile() {
    String junitFilePath = GlobalStateService.getInstance().getJunitFilePath();
    try (FileWriter writer = new FileWriter(junitFilePath)) {
      writer.write(junitReport.getJunitReport());
    } catch (IOException ex) {
      loggerService.printLogs(" Error while creating the Junit File : " + ex.getMessage());
    }
  }

  private List<String> createTRCLICommand(String pathToExe) throws Exception {
    String junitFile = GlobalStateService.getInstance().getJunitFilePath();

    // Check if the executable file exists
    File exeFile = new File(pathToExe);
    if (!exeFile.exists()) {
      loggerService.printLogs(" File doesn't exist at the location : " + pathToExe);
    }

    String testrailProjectName;
    String testrailRunId = runTimeDataProvider.getRuntimeData("TEST_RAIL_RUN_ID", ExecutionHierarchy.ROOT_RUN);
    if (projectName != null) {
      testrailProjectName = projectName.getValue().toString();
    } else {
      testrailProjectName = testrailAPIClientService.getProjectName(cicdCredentials, testrailRunId);
    }

    String url = this.cicdCredentials.getUrl();
    String username = this.cicdCredentials.getUsername();
    String password = this.cicdCredentials.getPassword();
    String runTitle = this.cicdCredentials.getCicdExecutionName();

    if (testrailRunId == null) {
      return Arrays.asList(
        pathToExe,
        "-h", url,
        "--insecure",
        "--project", testrailProjectName,
        "-u", username,
        "-p", password,
        "parse_junit",
        "--title", runTitle,
        "-f", junitFile
      );
    } else {
      return Arrays.asList(
        pathToExe,
        "-h", url,
        "--insecure",
        "--project", testrailProjectName,
        "-u", username,
        "-p", password,
        "parse_junit",
        "--run-id", testrailRunId,
        "-f", junitFile);
    }
  }

  private void createAndUploadAddonOutputZip() {
    try {
      zipService.createZipFile(runResult.getId());
      String statusCode = uploadToStorageService.uploadToStorage(cicdCredentials);
      loggerService.printLogs("Zip File Upload Status : " + statusCode);
    } catch (IOException e) {
      loggerService.printLogs("Error while creating and uploading zip : " + e.getMessage());
    }
  }

  private void cleanUp() throws IOException {
    FileUtils.deleteDirectory(new File(GlobalStateService.getInstance().getWorkingDirectory()));
  }

}
