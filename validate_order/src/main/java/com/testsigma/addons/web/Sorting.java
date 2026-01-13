package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Action(
        actionText = "Verify if the column data is in Ascending/Descending order",
        description = "Verifies if the data inside the column is sorted in ascending or descending order",
        applicationType = ApplicationType.WEB
)
public class Sorting extends WebAction {

    @TestData(
            reference = "Ascending/Descending",
            allowedValues = {"ascending", "descending"}
    )
    private com.testsigma.sdk.TestData operator;

    @Element(reference = "column")
    private com.testsigma.sdk.Element tableElement;

    @Override
    public Result execute() throws NoSuchElementException {

        try {
            String order = operator.getValue().toString().toLowerCase();
            logger.info("Sorting order selected: " + order);

            List<WebElement> elements = driver.findElements(tableElement.getBy());

            if (elements.isEmpty()) {
                setErrorMessage("The column data is empty. Sorting cannot be verified.");
                logger.warn("No elements found for sorting validation.");
                return Result.FAILED;
            }

            // Actual data from UI
            List<String> actualList = extractText(elements);

            // Expected sorted data
            List<String> expectedList = new ArrayList<>(actualList);
            Collections.sort(expectedList);

            if ("descending".equals(order)) {
                Collections.reverse(expectedList);
            }

            logger.info("Actual Column Data   : " + actualList);
            logger.info("Expected Sorted Data : " + expectedList);

            Assert.assertEquals(
                    actualList,
                    expectedList,
                    "Column data is not sorted in " + order + " order"
            );

            setSuccessMessage(
                    "Assertion passed. The column is sorted in " + order + " order."
            );
            return Result.SUCCESS;

        } catch (AssertionError ae) {
            logger.warn(ae.getMessage());
            setErrorMessage(
                    "Sorting verification failed. Column is not sorted as expected."
            );
            return Result.FAILED;

        } catch (Exception e) {
            logger.warn("Exception occurred while verifying sorting " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(
                    "Exception occurred while verifying sorting: " + ExceptionUtils.getMessage(e)
            );
            return Result.FAILED;
        }
    }

    private List<String> extractText(List<WebElement> elements) {
        List<String> data = new ArrayList<>();
        for (WebElement element : elements) {
            data.add(element.getText().trim());
        }
        return data;
    }
}
