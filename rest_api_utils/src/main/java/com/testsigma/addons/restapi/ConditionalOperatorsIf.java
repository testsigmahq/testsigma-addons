package com.testsigma.addons.restapi;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if test-data-1 operator test-data-2", description = "This is a numeric operation that finds relation between two numeric value", actionType = StepActionType.IF_CONDITION, applicationType = ApplicationType.REST_API)
public class ConditionalOperatorsIf extends RestApiAction {

	@TestData(reference = "test-data-1")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "operator", allowedValues = { ">", "<", ">=", "<=", "==" })
	private com.testsigma.sdk.TestData operator;
	@TestData(reference = "test-data-2")
	private com.testsigma.sdk.TestData testData2;

	@Override
	public Result execute() throws NoSuchElementException {
		String operatorString = operator.getValue().toString();
		double a = Double.valueOf(testData1.getValue().toString());
		double b = Double.valueOf(testData2.getValue().toString());
		Result result = Result.SUCCESS;
		switch (operatorString) {
		case ">":
			if (a > b) {
                logger.info("Passed " + a + " is > " + b);
				setSuccessMessage("Passed " + a + " is > " + b);
				System.out.print("Success");
			} else {
				result = Result.FAILED;
                logger.warn("Validation failed: " + a + " is not > " + b);
				setErrorMessage("Validation failed: " + a + " is not > " + b);
				System.out.println("failure");
			}

			break;
		case "<":
			if (a < b) {
                logger.info("Passed " + a + " is < " + b);
				setSuccessMessage("Passed " + a + " is < " + b);
				System.out.print("Success");
			} else {
				result = Result.FAILED;
                logger.warn("Validation failed: " + a + " is not < " + b);
				setErrorMessage("Validation failed: " + a + " is not < " + b);
				System.out.println("failure");
			}

			break;
		case ">=":
			if (a >= b) {
                logger.info("Passed " + a + " is >= " + b);
				setSuccessMessage("Passed " + a + " is >= " + b);
				System.out.print("Success");
			} else {
				result = Result.FAILED;
                logger.warn("Validation failed: " + a + " is not >= " + b);
				setErrorMessage("Validation failed:" + a + " is not >= " + b);
				System.out.println("failure");
			}

			break;
		case "<=":
			if (a <= b) {
                logger.info("Passed " + a + " is <= " + b);
				setSuccessMessage("Passed " + a + " is <= " + b);
				System.out.print("Success");
			} else {
				result = Result.FAILED;
                logger.warn("Validation failed: " + a + " is not <= " + b);
				setErrorMessage("Validation failed: " + a + " is not <= " + b);
				System.out.println("failure");
			}

			break;
		case "==":
			if (a == b) {
                logger.info("Passed " + a + " is == " + b);
				setSuccessMessage("Passed " + a + " is == " + b);
				System.out.print("Success");
			} else {
				result = Result.FAILED;
                logger.warn("Validation failed: " + a + " is != " + b);
				setErrorMessage("Validation failed: " + a + " is != " + b);
				System.out.println("failure");
			}

			break;
		}

		return result;
	}
}
