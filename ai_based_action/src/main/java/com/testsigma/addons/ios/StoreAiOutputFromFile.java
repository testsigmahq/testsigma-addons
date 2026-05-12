package com.testsigma.addons.ios;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
@Action(actionText = "Ai: Store the AI output for extracted using prompt ai-prompt-text-input on file file-path into a runtime variable runtime-variable",
        description = "Sends the specified file to AI with the given prompt to extract specific content or values " +
                "from the file, and stores the result into a runtime variable. " +
                "Supports (PDF, images, docx). " +
                "Use natural language to describe what content to extract (e.g. 'the invoice total', 'all table rows').",
        displayName = "Ai: Store AI output from file",
        applicationType = ApplicationType.IOS)
public class StoreAiOutputFromFile extends IOSAction {

    @TestData(reference = "ai-prompt-text-input")
    private com.testsigma.sdk.TestData aiPromptTextInput;

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @com.testsigma.sdk.annotation.RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @AI
    private com.testsigma.sdk.AI ai;

    @Override
    public Result execute() {
        logger.info("=== StoreAiOutputFromFile (iOS): Starting ===");
        Set<File> tempFiles = new HashSet<>();

        try {
            String prompt       = aiPromptTextInput.getValue().toString().trim();
            String path         = filePath.getValue().toString().trim();
            String variableName = runtimeVariable.getValue().toString();
            logger.info("Prompt: " + prompt + " | File: " + path + " | Variable: " + variableName);

            File file = resolveFile(path, tempFiles);

            String aiResponse = AiActionUtils.invokeAiWithFiles(
                    ai, Collections.singletonList(file), AiActionUtils.EXTRACT_FROM_FILE_PROMPT, prompt, logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get extraction response from AI (contact support)");
                return Result.FAILED;
            }

            boolean found      = responseNode.path("found").asBoolean(false);
            String output      = responseNode.path("output").asText("");
            int confidence     = responseNode.path("confidence").asInt(0);
            String description = responseNode.path("description").asText("");

            logger.info(String.format(
                    "AI extraction result — found=%b | output='%s' | confidence=%d | desc='%s'",
                    found, output, confidence, description));

            if (found) {
                runTimeData.setKey(variableName);
                runTimeData.setValue(output);
                setSuccessMessage(String.format(
                        "Stored '%s' into variable '%s' | confidence=%d | %s",
                        output, variableName, confidence, description));
                return Result.SUCCESS;
            } else {
                setErrorMessage(String.format(
                        "Could not extract output for prompt '%s' | confidence=%d | %s",
                        prompt, confidence, description));
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to store AI output from file. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            tempFiles.forEach(AiActionUtils::deleteQuietly);
        }
    }

    private File resolveFile(String path, Set<File> tempFiles) throws Exception {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String ext  = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : ".tmp";
            String base = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : "ai_file";
            File tmp = File.createTempFile(base, ext);
            try (InputStream in = new URL(path).openStream()) {
                Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("Downloaded " + path + " → " + tmp.getAbsolutePath());
            tempFiles.add(tmp);
            return tmp;
        }
        return new File(path);
    }
}
