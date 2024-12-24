package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
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

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

@Data
@Action(actionText = "Verify4 if the column data is in Ascending/Descending order with specific format dateformat",
		description = "Verifies if the data inside the column is in ascending or descending order with date format",
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

		switch (operatorString) {
			case "ascending":
				try {
					List<WebElement> wb = driver.findElements(tableElement.getBy());
					List<String> beforesort = new ArrayList<>();
					for (WebElement e : wb) {
						String text = e.getText().trim();
						if (!text.isEmpty()) {
							beforesort.add(text);
						}
					}

					List<String> aftersort = new ArrayList<>(beforesort);
					SortingDate.DateComparator comparator = new SortingDate.DateComparator(formatdata);
					Collections.sort(aftersort, comparator);

					if (isSorted(beforesort, aftersort, true)) {
						logger.info("After sort data  " + aftersort);
						logger.info("Before sort data  " + beforesort);
						setSuccessMessage("Assertion passed, the column is in ascending order");
					} else {
						result = com.testsigma.sdk.Result.FAILED;
						logger.warn("After sort data  " + aftersort);
						logger.warn("Before sort data  " + beforesort);
						setErrorMessage("Assertion failed, the column is not in ascending order");
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.debug(e.getMessage() + e.getCause());
					result = com.testsigma.sdk.Result.FAILED;
					setErrorMessage("Sorting operation failed. Please check if the column is sorted in ascending order.");
				}
				break;

			case "descending":
				try {
					List<WebElement> wb = driver.findElements(tableElement.getBy());
					List<String> beforesort = new ArrayList<>();
					for (WebElement e : wb) {
						String text = e.getText().trim();
						if (!text.isEmpty()) {
							beforesort.add(text);
						}
					}

					List<String> aftersort = new ArrayList<>(beforesort);
					SortingDate.DateComparator comparator = new SortingDate.DateComparator(formatdata);
					Collections.sort(aftersort, comparator);
					Collections.reverse(aftersort);

					if (isSorted(beforesort, aftersort, false)) {
						logger.info("After sort data  " + aftersort);
						logger.info("Before sort data  " + beforesort);
						setSuccessMessage("Assertion passed, the column is in descending order");
					} else {
						result = com.testsigma.sdk.Result.FAILED;
						logger.warn("After sort data  " + aftersort);
						logger.warn("Before sort data  " + beforesort);
						setErrorMessage("Assertion failed, the column is not in descending order");
					}

				} catch (Exception e) {
					e.printStackTrace();
					logger.debug(e.getMessage() + e.getCause());
					result = com.testsigma.sdk.Result.FAILED;
					setErrorMessage("Sorting operation failed. Please check if the column is sorted in descending order.");
				}
				break;
		}

		return result;
	}

	public class DateComparator implements Comparator<String> {
		private SimpleDateFormat dateFormat;

		public DateComparator(String format) {
			this.dateFormat = new SimpleDateFormat(format);
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

	private boolean isSorted(List<String> original, List<String> sorted, boolean ascending) throws ParseException {
		for (int i = 0; i < original.size(); i++) {
			Date date1 = new SimpleDateFormat(format.getValue().toString()).parse(original.get(i));
			Date date2 = new SimpleDateFormat(format.getValue().toString()).parse(sorted.get(i));

			if (ascending) {
				if (date1.compareTo(date2) > 0) {
					return false;
				}
			} else {
				if (date1.compareTo(date2) < 0) {
					return false;
				}
			}
		}
		return true;
	}
}