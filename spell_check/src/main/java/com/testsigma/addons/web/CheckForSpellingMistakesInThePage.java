package com.testsigma.addons.web;


import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.openqa.selenium.By;

import java.util.List;

@Data
@Action(actionText = "Check for the spelling mistakes in the current page",
        description = "This action checks for the spelling mistakes in the current page.",
        applicationType = ApplicationType.WEB)
public class CheckForSpellingMistakesInThePage extends WebAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;

        // get the texts from the current page.
        String allExtractedText = driver.findElement(By.tagName("body")).getText().trim();

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
                    logger.warn("Spelling issues found:\n" + issues);
                    result = Result.FAILED;
                    setErrorMessage("Found <b>" + spellMismatchCount + "</b> spelling issues:\n" + issues);
                }
            }
            logger.info("No spelling mistakes found.");
            setSuccessMessage("No spelling mistakes found.");

        } catch (Exception e) {
            logger.warn("Error during spell checking " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Exception occurred while checking spelling: " + e.getMessage());
        }
        return result;
    }
}