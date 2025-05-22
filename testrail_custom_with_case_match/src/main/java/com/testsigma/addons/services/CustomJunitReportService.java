package com.testsigma.addons.services;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;

public class CustomJunitReportService {

  private final LoggerService loggerService = new LoggerService();

  public void generateCustomJunit(String templateUrl, String apiKey, Long runId) throws Exception {

    GlobalStateService stateService = GlobalStateService.getInstance();
    String workDirectory = stateService.getWorkingDirectory();

    // download the report template
    String templatePath = workDirectory + "/custom_report_template.xml";
    FileUtils.copyURLToFile(new URI(templateUrl).toURL(), new File(templatePath));
    loggerService.printLogs("Downloaded custom junit file");

    // get jar file path
    String workingDirectory = GlobalStateService.getInstance().getWorkingDirectory();
    String jarPath = workingDirectory + "/custom-report-0.0.1-SNAPSHOT.jar";
    downloadCustomReportJar(jarPath);
    loggerService.printLogs("Jar file path: " + jarPath);

    String serverURL = System.getProperty("TS_APP_SERVER_URL") + "/";

    // build the command to run the JAR file with the additional arguments
    ProcessBuilder processBuilder = new ProcessBuilder(
      stateService.getJavaPath(),
      "-jar",
      jarPath,
      "--config.plan.runId=" + runId,
      "--config.apiKey=" + apiKey,
      "--config.template.location=" + templatePath,
      "--config.report.output.file=" + stateService.getJunitFilePath(),
      "--config.report.type=JUNIT",
      "--config.baseURL=" + serverURL
    );
    loggerService.printLogs("Executing Jar command " + processBuilder.command().toString());

    // execute the process
    Process process = processBuilder.start();

    // wait for the process to complete
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("Error generating JUnit report. Exit code: " + exitCode);
    }
  }

  private void downloadCustomReportJar(String jarPath) throws Exception {
    String url = "https://eks-common-bucket.s3.us-east-1.amazonaws.com/custom-report/custom-report-0.0.1-SNAPSHOT.jar";
    FileUtils.copyURLToFile(new URI(url).toURL(), new File(jarPath));
  }

}
