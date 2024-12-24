package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		String operatorString = operator.getValue().toString();
		String formatdata = format.getValue().toString();

		try {
			List<WebElement> wb = driver.findElements(tableElement.getBy());

			List<String> beforeSort = new ArrayList<>();
			for (WebElement e : wb) {
				beforeSort.add(e.getText());
			}

			List<String> afterSort = new ArrayList<>(beforeSort);
			DateComparator comparator = new DateComparator(formatdata);

			boolean isAscending = operatorString.equalsIgnoreCase("ascending");

			if (isAscending) {
				Collections.sort(afterSort, comparator); // Sort in ascending for ascending case
			} else {
				Collections.sort(afterSort,Collections.reverseOrder(comparator)); //Sort in descending for descending case
			}

			if (isSorted(beforeSort, afterSort, isAscending)) {
				logger.info("After sort data: " + afterSort);
				logger.info("Before sort data: " + beforeSort);
				setSuccessMessage("Assertion passed, the column is in " + operatorString + " order");

			} else {
				result = com.testsigma.sdk.Result.FAILED;
				logger.warn("After sort data: " + afterSort);
				logger.warn("Before sort data: " + beforeSort);
				setErrorMessage("Assertion not passed, the column is not in " + operatorString + " order");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage() + e.getCause());
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Sorting Operation failed. Please check if the column is sorted in " + operatorString);
		}
		return result;
	}

	public class DateComparator implements Comparator<String> {
		private SimpleDateFormat dateFormat;

		public DateComparator(String format) {
			this.dateFormat = new SimpleDateFormat(format);
		}

		public int compare(String s1, String s2) {
			try {
				Date d1 = dateFormat.parse(s1);
				Date d2 = dateFormat.parse(s2);
				return d1.compareTo(d2);
			} catch (ParseException e) {
				e.printStackTrace();
				return 0; // Or throw an exception if parsing fails are critical
			}
		}
	}

	private boolean isSorted(List<String> original, List<String> sorted, boolean ascending) throws ParseException {
		if (original.size() != sorted.size()) {
			return false; // If size are different then it is not sorted
		}

		for (int i = 0; i < original.size(); i++) {
			if (!original.get(i).equals(sorted.get(i))) {
				return false;
			}
		}
		return true; // If loop completes without returning false, then they are the same.

	}
}
