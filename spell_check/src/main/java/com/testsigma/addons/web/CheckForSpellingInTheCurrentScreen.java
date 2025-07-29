package com.testsigma.addons.web;


import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import com.testsigma.sdk.annotation.OCR;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;


import java.io.File;
import java.util.List;

@Data
@Action(actionText = "Check for the spelling mistakes in the current screen",
        description = "This action checks for the spelling mistakes in the current page.",
        applicationType = ApplicationType.WEB)
public class CheckForSpellingInTheCurrentScreen extends WebAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @Override
    public com.testsigma.sdk.Result execute () {
        com.testsigma.sdk.Result result = Result.SUCCESS;
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        logger.info("Screenshot taken");

        // Create OCRImage object from the screenshot
        OCRImage imageObj = new OCRImage();
        imageObj.setOcrImageFile(screenshot);

        // Extract text points from the image
        List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
        logger.info("Extracted text from image: " + textPoints);

        // Log all extracted text for debugging
        String allExtractedText = textPoints.stream()
                .map(OCRTextPoint::getText)
                .reduce("", String::concat);
        logger.info("All extracted text combined: '" + allExtractedText + "'");


        try {
            // Create LanguageTool instance for American English
            JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());

            // Check the combined extracted text
            List<RuleMatch> matches = langTool.check(allExtractedText);

            if (!matches.isEmpty()) {
                StringBuilder issues = new StringBuilder();
                int spellMismatchCount = 0;
                for (RuleMatch match : matches) {
                    // add only spelling mistakes
                    if (match.getRule().getCategory().getName().equalsIgnoreCase("Spelling")) {
                        issues.append(match);
                        spellMismatchCount++;
                    }
                }
                if (spellMismatchCount != 0) {
                    logger.warn("Spelling/Grammar issues found:\n" + issues);
                    result = Result.FAILED;
                    setErrorMessage("Found <b>" + spellMismatchCount + "</b> spelling/grammar issues:\n" + issues);
                }
            }
            logger.info("No spelling/grammar issues found.");
            setSuccessMessage("No spelling/grammar issues found.");

        } catch (Exception e) {
            logger.warn("Error during spell checking " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Exception occurred while checking spelling: " + e.getMessage());
        }
        return result;
    }
}


        /*AIRequest aiRequest = new AIRequest();
        aiRequest.setPrompt(prompt + textPoints);
        aiRequest.setModel("gpt-4o");

        String aiResponse = ai.invokeAI(aiRequest);
        logger.info("AI response: {}" + aiResponse);
        if(aiResponse.equalsIgnoreCase("no mistakes")) {
            logger.info("No spelling mistakes found in the current page.");
            setSuccessMessage("No spelling mistakes found in the current page.");
        } else {
            logger.info("Spelling mistakes found: " + aiResponse);
            setErrorMessage("Found spelling mistakes " + aiResponse);
            result = Result.FAILED;
        }*/