package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Ai: Verify if the text of element element-locator matches the prompt verification-query",
        description = "Extracts the visible text of the element identified by element-locator and asks AI to " +
                "verify whether it matches the description in verification-query. The step passes if AI confirms " +
                "the match; fails otherwise. Use natural language to describe what the text should be or contain " +
                "(e.g. 'the price is $49.99', 'a valid email address', 'contains the word Success').",
        displayName = "Ai: Verify element text matches prompt",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyIfElementTextMatchesThePromptUsingAi extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element elementLocator;

    @TestData(reference = "verification-query")
    private com.testsigma.sdk.TestData verificationQuery;

    @AI
    private com.testsigma.sdk.AI ai;

    @Override
    public Result execute() {
        logger.info("=== VerifyIfElementTextMatchesThePromptUsingAi (Web): Starting ===");

        try {
            String query = verificationQuery.getValue().toString();
            logger.info("Verification query: " + query);

            WebElement webElement;
            try {
                webElement = elementLocator.getElement();
            } catch (NoSuchElementException e) {
                logger.info("Element not found: " + e.getMessage());
                setErrorMessage(String.format("Element not found using locator %s::%s. %s",
                        elementLocator.getBy(), elementLocator.getValue(), e.getMessage()));
                return Result.FAILED;
            }

            String elementText;
            try {
                elementText = AiActionUtils.extractElementText(webElement);
            } catch (Exception e) {
                logger.info("Failed to extract text from element: " + e.getMessage());
                setErrorMessage(String.format(
                        "Failed to extract text from the element with locator %s::%s. Error: %s",
                        elementLocator.getBy(), elementLocator.getValue(), e.getMessage()));
                return Result.FAILED;
            }

            if (elementText == null || elementText.trim().isEmpty()) {
                setErrorMessage(String.format(
                        "Could not extract any text from the element with locator %s::%s (text, value and content-desc were all empty).",
                        elementLocator.getBy(), elementLocator.getValue()));
                return Result.FAILED;
            }
            logger.info("Extracted element text: '" + elementText + "'");

            String combinedQuery = "ACTUAL ELEMENT TEXT: \"" + elementText + "\"\n\nVERIFICATION PROMPT: " + query;

            String aiResponse;
            try {
                aiResponse = AiActionUtils.invokeAiTextOnly(ai, AiActionUtils.VERIFY_ELEMENT_TEXT_PROMPT, combinedQuery, logger);
            } catch (Exception e) {
                logger.info("Failed to get response from AI: " + e.getMessage());
                setErrorMessage("Failed to get response from AI. Error: " + e.getMessage());
                return Result.FAILED;
            }

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get a valid verification response from AI (contact support)");
                return Result.FAILED;
            }

            boolean verified   = responseNode.path("verified").asBoolean(false);
            int confidence     = responseNode.path("confidence").asInt(0);
            String description = responseNode.path("description").asText("");

            logger.info(String.format("AI verification result — verified=%b | confidence=%d | desc: '%s'",
                    verified, confidence, description));

            if (verified) {
                setSuccessMessage(String.format(
                        "Successfully verified that element text '%s' matches '%s' | confidence=%d | %s",
                        elementText, query, confidence, description));
                return Result.SUCCESS;
            } else {
                setErrorMessage(String.format(
                        "Verification FAILED — element text '%s' does not match '%s' | confidence=%d | %s",
                        elementText, query, confidence, description));
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to verify element text using AI. Error: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
