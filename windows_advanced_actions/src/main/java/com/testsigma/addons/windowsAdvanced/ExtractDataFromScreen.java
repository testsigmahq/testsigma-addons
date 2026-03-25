package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.AIRequest;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.*;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;


@Action(actionText = "AI: Extract the data and store in a variable Query-to-extract-data Variable-Name",
        description = "Store data from screen based on the query",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "AI: Extract the data and store in a variable",
        useCustomScreenshot = true)
public class ExtractDataFromScreen extends WindowsAdvancedAction {


    @TestData(reference = "Query-to-extract-data")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "Variable-Name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;
    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;
    @AI
    private com.testsigma.sdk.AI ai;

    final String prompt = "You are given with a screenshot of a desktop, you have to extract data based on user queries." +
            " You MUST respond in the following JSON format only:" +
            " {\"isQueryRelated\": boolean, \"extracted Text\": \"string\", \"additional data\": \"string\"}" +
            " Rules:" +
            " - Set 'isQueryRelated' to true if the query is related to the screenshot content, false otherwise." +
            " - 'extracted Text' should contain ONLY the raw data requested by the user, no additional context or explanation." +
            " - For example,  - If user asks for a number, give only the number. If for text, give only that text." +
            " - Use 'additional data' for any contextual information, explanations, or additional details." +
            " - If the query is not related to the screenshot, set 'isQueryRelated' to false, 'extracted Text' to empty string, and explain in 'additional data'." +
            " - If the requested data is not present in the screenshot, set 'isQueryRelated' to true, 'extracted Text' to empty string, and explain in 'additional data'." +
            " - Ensure your response is valid JSON format only, no additional text.";

    @Override
    protected com.testsigma.sdk.Result execute() {
        Result result = Result.SUCCESS;
        logger.info("=== Store Data From Screen: Starting Execution ===");

        String userQuery = testData.getValue().toString();
        String fullPrompt = prompt + " The query is: " + testData.getValue().toString();

        Robot robot = null;
        try {
            robot = new Robot();


            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

            // Save the screenshot to a temporary file
            File screenshotFile = ScreenshotUtils.saveScreenshotToFile(screenCapture, "click_text_screenshot");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile , logger);

            // Add the screenshot file
            AIRequest aiRequest = new AIRequest();
            aiRequest.setPrompt(fullPrompt);
            aiRequest.setFiles(List.of(screenshotFile));
            aiRequest.setModel("gpt-4.1");

            // Invoke AI
            String aiResponse = ai.invokeAI(aiRequest);
            logger.info("AI response: " + aiResponse);

            // Parse the JSON response
            AIResponse parsedResponse = parseAIResponse(aiResponse);

            if (parsedResponse != null) {
                if (parsedResponse.isQueryRelated) {
                    if (!parsedResponse.extractedText.trim().isEmpty()) {
                        // Store the extracted text in the runtime variable
                        logger.info("additional data : " + parsedResponse.additionalData);
                        runTimeData.setKey(runtimeVariable.getValue().toString());
                        runTimeData.setValue(parsedResponse.extractedText);
                        setSuccessMessage("Data extracted and stored in variable " + runtimeVariable.getValue().toString() +
                                ": '" + parsedResponse.extractedText + "'" +
                                (parsedResponse.additionalData.isEmpty() ? "" : " (Additional info: " + parsedResponse.additionalData + ")"));
                    } else {
                        setErrorMessage("Requested data not found in screenshot" +
                                (parsedResponse.additionalData.isEmpty() ? "" : ": " + parsedResponse.additionalData));
                        return Result.FAILED;
                    }
                } else {
                    setErrorMessage("Query not related to screenshot" +
                            (parsedResponse.additionalData.isEmpty() ? "" : ": " + parsedResponse.additionalData));
                    return Result.FAILED;
                }
            } else {
                setErrorMessage("Failed to get the response from AI");
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Error during execution: " + e.getMessage());
            setErrorMessage("Error during execution: " + e.getMessage());
            return Result.FAILED;
        }

        return Result.SUCCESS;
    }

    /**
     * Parses the AI response JSON
     * @param aiResponse The raw AI response
     * @return Parsed AIResponse object or null if parsing fails
     */
    private AIResponse parseAIResponse(String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(aiResponse);

            AIResponse response = new AIResponse();
            response.isQueryRelated = jsonNode.get("isQueryRelated").asBoolean();
            response.extractedText = jsonNode.get("extracted Text").asText("");
            response.additionalData = jsonNode.get("additional data").asText("");

            return response;
        } catch (Exception e) {
            logger.warn("Failed to parse AI response JSON: " + e.getMessage());
            logger.warn("Raw AI response: " + aiResponse);
            return null;
        }
    }

    /**
     * Saves the screenshot to a temporary file
     * @param screenshot The captured screenshot
     * @param fileName The base filename
     * @return The temporary file
     * @throws Exception if file creation fails
     */
    private File saveScreenshotToFile(BufferedImage screenshot, String fileName) throws Exception {
        try {
            File tempFile = File.createTempFile(fileName, ".png");
            ImageIO.write(screenshot, "PNG", tempFile);
            return tempFile;
        } catch (Exception e) {
            logger.debug("Failed to save screenshot to file: " + e.getMessage());
            throw new RuntimeException("Unable to save screenshot for AI processing.", e);
        }
    }

    /**
     * Inner class to hold parsed AI response
     */
    private static class AIResponse {
        boolean isQueryRelated;
        String extractedText;
        String additionalData;
    }
}