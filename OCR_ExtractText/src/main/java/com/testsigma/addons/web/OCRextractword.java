package com.testsigma.addons.web;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.OCRImage;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;

import lombok.Data;

@Data
@Action(actionText = "Extract text between start_phrase and end_phrase,occurrence of phrase phrase_index and store into a variable runtime_testdata",
description = "Using OCR extract the text from ",
applicationType = ApplicationType.WEB)
public class OCRextractword extends WebAction {
	@OCR
	private com.testsigma.sdk.OCR ocr;

	@TestData(reference = "start_phrase")
	private com.testsigma.sdk.TestData testdata1;

	@TestData(reference = "end_phrase")
	private com.testsigma.sdk.TestData testdata2;
	
	@TestData(reference = "phrase_index")
	private com.testsigma.sdk.TestData testdata3;
	
	@TestData(reference = "runtime_testdata" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testdata4;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	protected Result execute() {
		Result result = Result.SUCCESS;
		try {
			logger.info("Taking screenshot");
            File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            logger.info("Taking screenshot completed");
            OCRImage imageObj = new OCRImage();
            imageObj.setOcrImageFile(screenshot);
			List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
			logger.info("Extracted text from image:" + textPoints.toString());
			String startphrase = testdata1.getValue().toString();
			String endphrase = testdata2.getValue().toString();
	        int phaseindex = Integer.parseInt(testdata3.getValue().toString());
	        
	        String extractText = findTextBetweenPhrases(textPoints, startphrase, endphrase, phaseindex);
       
                runTimeData.setKey(testdata4.getValue().toString());
    			runTimeData.setValue(extractText);

    			setSuccessMessage("The Text was extracted and stored into a variable" + " : " +testdata4.getValue().toString() + " : and the value is: " +extractText);
    			logger.info("The Text was extracted and stored into a variable" + " : " +testdata4.getValue().toString() + ": and the value is:" +extractText);
		}catch(Exception e) { 
			setErrorMessage("inside addon - " + ExceptionUtils.getStackTrace(e));
			result = Result.FAILED;
		}
		return result;
	}
	
	public String findTextBetweenPhrases(List<OCRTextPoint> textPoints, String startPhrase, String endPhrase, int targetOccurrence) {
        if (textPoints == null || textPoints.isEmpty()) {
            return null;
        }

        startPhrase = insertSpaces(startPhrase);
        endPhrase = insertSpaces(endPhrase);

        String[] startWords = startPhrase.split(" ");
        String[] endWords = endPhrase.split(" ");

        int startWordCount = startWords.length;
        int endWordCount = endWords.length;
        int occurrence = 0;

        for (int i = 0; i < textPoints.size(); i++) {
            boolean match = true;
            if (i + startWordCount > textPoints.size()) {
                break;
            }
            for (int j = 0; j < startWordCount; j++) {
                if (!textPoints.get(i + j).getText().equals(startWords[j])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                occurrence += 1;
                if (occurrence == targetOccurrence) {
                    int startIndex = i;

                    // Find end point
                    for (int k = i + startWordCount; k < textPoints.size(); k++) {
                        match = true;
                        if (k + endWordCount > textPoints.size()) {
                            break;
                        }
                        for (int l = 0; l < endWordCount; l++) {
                            if (!textPoints.get(k + l).getText().equals(endWords[l])) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            int endIndex = k + endWordCount - 1;
                            StringBuilder extractedText = new StringBuilder();
                            for (int m = startIndex + startWordCount; m < endIndex; m++) {
                                extractedText.append(textPoints.get(m).getText()).append(" ");
                            }
                            return extractedText.toString().trim();
                        }
                    }
                }
            }
        }
        return null;
    }

	private String insertSpaces(String sentence) {
		    String regex = "(\\W)";
		    Pattern pattern = Pattern.compile(regex);
		    Matcher matcher = pattern.matcher(sentence);
		    String modifiedSentence = matcher.replaceAll(" $1 ");
		    modifiedSentence = modifiedSentence.replaceAll("\\s+", " ");
		    modifiedSentence = modifiedSentence.trim();
		    logger.info("Inserting spaces at special characters : " + modifiedSentence);
		    return modifiedSentence;
		}
	}
