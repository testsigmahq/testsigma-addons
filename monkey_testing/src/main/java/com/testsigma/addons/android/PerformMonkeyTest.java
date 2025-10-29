package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import java.io.*;
        import java.util.*;

@Action(actionText = "Execute Monkey test with event-count events and capture logs",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = true)
public class PerformMonkeyTest extends AndroidAction {

    @TestData(reference = "event-count")
    private com.testsigma.sdk.TestData eventCount;

    @Override
    protected com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating Monkey Test execution");
        logger.debug("Event Count: " + eventCount.getValue());

        try {
            AndroidDriver androidDriver = (AndroidDriver) this.driver;

            // Get the package of the app under test from driver capabilities
            String pkgName = (String) androidDriver.getCapabilities().getCapability("appPackage");
            if (pkgName == null || pkgName.isEmpty()) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Cannot determine app package. Make sure the app is installed.");
                return result;
            }

            int events = Integer.parseInt(eventCount.getValue().toString());
            logger.info("Starting Monkey test for package: " + pkgName);

            // Execute Monkey test and capture logs
            List<String> logs = executeMonkeyWithRealTimeLog(androidDriver, pkgName, events);

            // Parse and analyze the logs
            MonkeyTestReport report = parseMonkeyLogs(logs);

            // Check for failures
            if (report.crashed) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Monkey test detected app crash. Crashes: " + report.crashDetails.size()
                        + "Crash details: " + String.join(", ", report.crashDetails));
                logger.warn("App crashed during Monkey test");
                logger.debug("Crash details: " + String.join(", ", report.crashDetails));
            } else if (report.anrOccurred) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Monkey test detected ANR (Application Not Responding)");
                logger.warn("ANR occurred during Monkey test");
                logger.debug("ANR details: " + String.join(", ", report.anrDetails));
            } else {
                setSuccessMessage(String.format(
                        "Monkey test completed successfully. Events injected: %d, Touch: %d, Keys: %d",
                        report.eventsInjected, report.touchEvents, report.keyEvents
                ));
                logger.info("Monkey test completed without crashes or ANRs");
            }

            // Log summary
            logger.info(report.toString());

        } catch (NumberFormatException e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.debug("Invalid event count format: " + eventCount.getValue());
            setErrorMessage("Event count must be a valid number");
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.debug("Error executing Monkey test: " + e.getMessage());
            setErrorMessage("Failed to execute Monkey test: " + e.getMessage());
        }

        return result;
    }

    private List<String> executeMonkeyWithRealTimeLog(AndroidDriver androidDriver, String pkgName, int events) {
        List<String> logLines = new ArrayList<>();
        try {
            String deviceSerial = (String) androidDriver.getCapabilities().getCapability("udid");
            if (deviceSerial == null || deviceSerial.isEmpty()) {
                deviceSerial = (String) androidDriver.getCapabilities().getCapability("deviceName");
            }

            logger.info("Executing Monkey on device: " + deviceSerial);

            // Build adb monkey command
            List<String> command = new ArrayList<>(Arrays.asList(
                    "adb",
                    "-s", deviceSerial,
                    "shell",
                    "monkey",
                    "-p", pkgName,
                    "-v", "-v", "-v",
                    "--throttle", "300",
                    "--pct-touch", "40",
                    "--pct-motion", "25",
                    "--pct-nav", "15",
                    "--pct-majornav", "10",
                    "--pct-syskeys", "2",
                    "--pct-appswitch", "8",
                    "--ignore-crashes",
                    "--ignore-timeouts",
                    "--ignore-security-exceptions",
                    String.valueOf(events)
            ));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
//                    logger.debug("Monkey: " + line);
                    logLines.add(line);
                }
            }

            int exitCode = process.waitFor();
            logger.info("Monkey process completed with exit code: " + exitCode);

        } catch (Exception e) {
            logger.warn("Error executing Monkey command: " + e.getMessage());
            logLines.add("Error: " + e.getMessage());
        }
        return logLines;
    }

    private MonkeyTestReport parseMonkeyLogs(List<String> logs) {
        MonkeyTestReport report = new MonkeyTestReport();

        for (String line : logs) {
            if (line.contains("Events injected:")) {
                report.eventsInjected = extractNumber(line);
            } else if (line.contains("Sending Touch") || line.contains(":Sending Touch")) {
                report.touchEvents++;
            } else if (line.contains("Sending Trackball")) {
                report.trackballEvents++;
            } else if (line.contains("Sending Key") || line.contains(":Sending Key")) {
                report.keyEvents++;
            } else if (line.contains("// CRASH:") || line.contains("CRASH")) {
                report.crashed = true;
                report.crashDetails.add(line);
            } else if (line.contains("ANR")) {
                report.anrOccurred = true;
                report.anrDetails.add(line);
            } else if (line.contains("Monkey finished")) {
                logger.info("Monkey test completed successfully");
            }
        }

        return report;
    }

    private int extractNumber(String line) {
        try {
            String[] parts = line.split(":");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1].trim());
            }
        } catch (Exception e) {
            logger.debug("Could not extract number from: " + line);
        }
        return 0;
    }

    private static class MonkeyTestReport {
        int eventsInjected = 0;
        int touchEvents = 0;
        int trackballEvents = 0;
        int keyEvents = 0;
        boolean crashed = false;
        boolean anrOccurred = false;
        List<String> crashDetails = new ArrayList<>();
        List<String> anrDetails = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Monkey Test Report:\n");
            sb.append("Events Injected: ").append(eventsInjected).append("\n");
            sb.append("Touch Events: ").append(touchEvents).append("\n");
            sb.append("Trackball Events: ").append(trackballEvents).append("\n");
            sb.append("Key Events: ").append(keyEvents).append("\n");
            sb.append("Crashed: ").append(crashed).append("\n");
            sb.append("ANR Occurred: ").append(anrOccurred).append("\n");

            if (!crashDetails.isEmpty()) {
                sb.append("\nCrash Details:\n");
                crashDetails.forEach(detail -> sb.append("  ").append(detail).append("\n"));
            }

            if (!anrDetails.isEmpty()) {
                sb.append("\nANR Details:\n");
                anrDetails.forEach(detail -> sb.append("  ").append(detail).append("\n"));
            }

            return sb.toString();
        }
    }
}