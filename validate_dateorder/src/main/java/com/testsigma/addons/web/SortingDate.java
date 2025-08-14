package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

@Data
@Action(actionText = "Verify if the column data is in Ascending/Descending order with specific format dateformat",
		description = "Verifies if the data inside the column is in ascending or descending order with dateformat",
		applicationType = ApplicationType.WEB)
public class SortingDate extends WebAction {


	@TestData(reference = "Ascending/Descending", allowedValues = {"ascending", "descending"})
	private com.testsigma.sdk.TestData operator;
	@Element(reference = "column")
	private com.testsigma.sdk.Element tableElement;
	@TestData(reference = "dateformat")
	private com.testsigma.sdk.TestData format;

	@Override
	public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        String order = operator.getValue().toString().trim().toLowerCase();
        String dateFormatPattern = format.getValue().toString().trim();

        try {
            List<WebElement> elements = driver.findElements(tableElement.getBy());
            if (elements.isEmpty()) {
                setErrorMessage("No elements found for the given locator");
                return Result.FAILED;
            }

            if (elements.size() < 2) {
                logger.warn("The given locator should have atleast 2 elements");
                setErrorMessage("The given locator should have atleast 2 elements");
                return Result.FAILED;
            }

            List<String> originalTexts = new ArrayList<>();
            for (WebElement e : elements) {
                originalTexts.add(e.getText().trim());
            }

            DateComparator comparator = new DateComparator(dateFormatPattern);

            List<String> sortedList = new ArrayList<>(originalTexts);
            sortedList.sort(comparator);
            if ("descending".equals(order)) {
                Collections.reverse(sortedList);
            }

            if (originalTexts.equals(sortedList)) {
                setSuccessMessage("Assertion passed, the column is in " + order + " order");
            } else {
                result = Result.FAILED;
                setErrorMessage("Assertion failed, the column is not in " + order + " order");
            }

            logger.info("Original Data: " + originalTexts);
            logger.info("Expected Sorted Data: " + sortedList);

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("Sorting Operation failed: " + ExceptionUtils.getMessage(e));
            logger.warn("Sorting Operation failed: " + ExceptionUtils.getStackTrace(e));
        }

        return result;
    }

    public static class DateComparator implements Comparator<String> {
        private final SimpleDateFormat dateFormat;

        public DateComparator(String format) {
            this.dateFormat = new SimpleDateFormat(format);
            this.dateFormat.setLenient(false);
        }

        @Override
        public int compare(String s1, String s2) {
            try {
                Date d1 = dateFormat.parse(s1);
                Date d2 = dateFormat.parse(s2);
                return d1.compareTo(d2);
            } catch (ParseException e) {
               e.printStackTrace();
            }

            return 0;
        }
    }
}
