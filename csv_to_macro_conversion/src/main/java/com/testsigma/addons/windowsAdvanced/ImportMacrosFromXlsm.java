package com.testsigma.addons.windowsAdvanced;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Data
@Action(
        actionText = "Import macros from source XLSM file source-file-path into target XLSM file target-file-path",
        description = "Exports all VBA modules (standard, class, and form) from the source XLSM file and imports " +
                "them into the target XLSM file using PowerShell COM automation. Existing modules with the same " +
                "name in the target are replaced. Requires Windows with Microsoft Excel installed and the " +
                "'Trust access to the VBA project object model' option enabled in Excel Trust Center settings. ",
        applicationType = ApplicationType.WINDOWS_ADVANCED
)
public class ImportMacrosFromXlsm extends WindowsAdvancedAction {

    @TestData(reference = "source-file-path")
    private com.testsigma.sdk.TestData sourceFilePath;

    @TestData(reference = "target-file-path")
    private com.testsigma.sdk.TestData targetFilePath;

    @Override
    protected Result execute() {
        String sourcePath = sourceFilePath.getValue().toString().trim();
        String targetPath = targetFilePath.getValue().toString().trim();

        logger.info("Initiating ImportMacrosFromXlsm action");
        logger.info("Source file: " + sourcePath);
        logger.info("Target file: " + targetPath);

        File sourceFile = new File(sourcePath);
        File targetFile = new File(targetPath);

        if (!sourceFile.exists()) {
            setErrorMessage("Source file not found: " + sourcePath);
            return Result.FAILED;
        }
        if (!sourceFile.getName().toLowerCase().endsWith(".xlsm")) {
            setErrorMessage("Source file must be a macro-enabled workbook (.xlsm): " + sourcePath);
            return Result.FAILED;
        }

        if (!targetFile.exists()) {
            setErrorMessage("Target file not found: " + targetPath);
            return Result.FAILED;
        }
        if (!targetFile.getName().toLowerCase().endsWith(".xlsm")) {
            setErrorMessage("Target file must be a macro-enabled workbook (.xlsm): " + targetPath);
            return Result.FAILED;
        }

        if (!validateXlsm(sourceFile, "Source")) return Result.FAILED;
        if (!validateXlsm(targetFile, "Target")) return Result.FAILED;

        try {
            String escapedSource = sourceFile.getAbsolutePath().replace("'", "''");
            String escapedTarget = targetFile.getAbsolutePath().replace("'", "''");

            String psScript = buildPowerShellScript(escapedSource, escapedTarget);

            logger.info("Launching PowerShell to import VBA modules...");
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-NonInteractive", "-NoProfile", "-Command", psScript);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    logger.info("[PowerShell] " + line);
                }
            }

            int exitCode = process.waitFor();
            String outputStr = output.toString().trim();

            if (exitCode != 0) {
                setErrorMessage("Macro import failed (exit code: " + exitCode + "). Output: " + outputStr);
                return Result.FAILED;
            }

            int importedCount = parseImportedCount(outputStr);


            setSuccessMessage("Successfully imported " + importedCount + " VBA module(s) from '" +
                    sourceFile.getName() + "' into '" + targetFile.getName() + "'. ");
            return Result.SUCCESS;

        } catch (IOException | InterruptedException e) {
            setErrorMessage("Failed to execute PowerShell script: " + e.getMessage());
            logger.warn("Error during macro import: " + e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            setErrorMessage("Unexpected error during macro import: " + e.getMessage());
            logger.warn("Unexpected error: " + e.getMessage());
            return Result.FAILED;
        }
    }

    private boolean validateXlsm(File file, String label) {
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            logger.info(label + " workbook is valid. Sheets: " + wb.getNumberOfSheets());
            return true;
        } catch (Exception e) {
            setErrorMessage(label + " file is not a valid XLSM workbook: " + e.getMessage());
            return false;
        }
    }

    private String buildPowerShellScript(String escapedSource, String escapedTarget) {
        return
            "$ErrorActionPreference = 'Stop';" +
            "$tempDir = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), 'vba_import_' + [System.Diagnostics.Process]::GetCurrentProcess().Id);" +
            "New-Item -ItemType Directory -Force -Path $tempDir | Out-Null;" +
            "$excel = New-Object -ComObject Excel.Application;" +
            "$excel.Visible = $false;" +
            "$excel.DisplayAlerts = $false;" +
            "try {" +
            "  $srcWb = $excel.Workbooks.Open('" + escapedSource + "');" +
            "  $exportedPaths = @();" +
            "  foreach ($comp in $srcWb.VBProject.VBComponents) {" +
            "    $ext = switch ($comp.Type) { 1 { '.bas' } 2 { '.cls' } 3 { '.frm' } default { $null } };" +
            "    if ($ext) {" +
            "      $exportPath = [System.IO.Path]::Combine($tempDir, $comp.Name + $ext);" +
            "      $comp.Export($exportPath);" +
            "      $exportedPaths += $exportPath;" +
            "      Write-Output ('Exported: ' + $comp.Name + $ext);" +
            "    }" +
            "  }" +
            "  $srcWb.Close($false);" +
            "  $tgtWb = $excel.Workbooks.Open('" + escapedTarget + "');" +
            "  $tgtVba = $tgtWb.VBProject;" +
            "  $importedCount = 0;" +
            "  foreach ($modulePath in $exportedPaths) {" +
            "    $moduleName = [System.IO.Path]::GetFileNameWithoutExtension($modulePath);" +
            "    try {" +
            "      $existing = $tgtVba.VBComponents.Item($moduleName);" +
            "      if ($existing.Type -ne 100) { $tgtVba.VBComponents.Remove($existing) }" +
            "    } catch { }" +
            "    $tgtVba.VBComponents.Import($modulePath);" +
            "    $importedCount++;" +
            "    Write-Output ('Imported: ' + $moduleName);" +
            "  }" +
            "  $tgtWb.Save();" +
            "  $tgtWb.Close($false);" +
            "  Write-Output ('SUCCESS:' + $importedCount);" +
            "} finally {" +
            "  $excel.Quit();" +
            "  [System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel) | Out-Null;" +
            "  [GC]::Collect();" +
            "  if (Test-Path $tempDir) { Remove-Item $tempDir -Recurse -Force }" +
            "}";
    }

    private int parseImportedCount(String psOutput) {
        for (String line : psOutput.split("\\r?\\n")) {
            if (line.startsWith("SUCCESS:")) {
                try {
                    return Integer.parseInt(line.substring("SUCCESS:".length()).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }
}
