package com.testsigma.addons.WindowsAdvanced;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Data
@Action(actionText = "Verify if test-data1 operator test-data2",
        description = "Verifies two values against the specified operator. Relational operators (<, >, <=, >=, ==, !=) "
                + "compare numerically when both values are numeric, otherwise lexicographically. "
                + "'contains' and 'does not contains' always operate on the raw text of both values.",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        actionType = StepActionType.WHILE_LOOP,
        displayName = "Verify if strings match the condition",
        useCustomScreenshot = false)
public class VerifyIfStringsMatchTheOperatorWhileLoop extends WindowsAdvancedAction {

    private static final String OP_EQ = "==";
    private static final String OP_NEQ = "!=";
    private static final String OP_LT = "<";
    private static final String OP_GT = ">";
    private static final String OP_LTE = "<=";
    private static final String OP_GTE = ">=";
    private static final String OP_LT_ALIAS = "less than";
    private static final String OP_GT_ALIAS = "greater than";
    private static final String OP_CONTAINS = "contains";
    private static final String OP_NOT_CONTAINS = "does not contains";

    private static final Set<String> SUPPORTED_OPERATORS = new HashSet<>(Arrays.asList(
            OP_EQ, OP_NEQ, OP_LT, OP_GT, OP_LTE, OP_GTE,
            OP_LT_ALIAS, OP_GT_ALIAS, OP_CONTAINS, OP_NOT_CONTAINS));

    /**
     * Accepts optional sign, integer/decimal forms (including leading-dot such as ".5")
     * and scientific notation. Anything matching this is safe for BigDecimal.
     */
    private static final String NUMERIC_PATTERN = "[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?";

    @TestData(reference = "test-data1")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "test-data2")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "operator",
            allowedValues = {"==", ">", "<", "<=", ">=", "!=", "less than", "greater than",
                    "contains", "does not contains"})
    private com.testsigma.sdk.TestData operator;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Verify If Strings Match The Operator: Starting Execution ===");

        if (testData1.getValue() == null || testData2.getValue() == null || operator.getValue() == null) {
            setErrorMessage("One or more input values are null");
            return Result.FAILED;
        }

        String str1 = testData1.getValue().toString().trim();
        String str2 = testData2.getValue().toString().trim();
        String op = operator.getValue().toString().trim().toLowerCase(Locale.ROOT);

        // Validate the operator BEFORE comparing, so the error message is not
        // overwritten by the generic "Comparison failed" message below.
        if (!SUPPORTED_OPERATORS.contains(op)) {
            setErrorMessage("Unsupported operator: '" + operator.getValue()
                    + "'. Supported operators: " + SUPPORTED_OPERATORS);
            // NOTE: in a WHILE_LOOP action this looks identical to a normal
            // "condition is false" exit. If the SDK exposes a distinct error
            // result, prefer it here so config mistakes are not mistaken for a
            // clean loop termination.
            return Result.FAILED;
        }

        boolean comparisonResult;

        if (isTextualOperator(op)) {
            // 'contains' is inherently textual. Always run it on the raw input so
            // numbers are never round-tripped through double (which would add ".0",
            // switch to E-notation past 1.0E7, and strip trailing/leading zeros).
            comparisonResult = compareStrings(str1, str2, op);
        } else if (isNumeric(str1) && isNumeric(str2)) {
            comparisonResult = compareNumbers(str1, str2, op);
        } else {
            comparisonResult = compareStrings(str1, str2, op);
        }

        Result result;
        if (comparisonResult) {
            result = Result.SUCCESS;
            setSuccessMessage("Comparison succeeded: " + str1 + " " + op + " " + str2);
        } else {
            result = Result.FAILED;
            setErrorMessage("Comparison failed: " + str1 + " " + op + " " + str2);
        }

        logger.info("=== Verify If Strings Match The Operator: Result = " + result + " ===");
        return result;
    }

    private boolean isTextualOperator(String op) {
        return OP_CONTAINS.equals(op) || OP_NOT_CONTAINS.equals(op);
    }

    private boolean isNumeric(String str) {
        return str != null && !str.isEmpty() && str.matches(NUMERIC_PATTERN);
    }

    /**
     * BigDecimal rather than double: exact decimal semantics, no precision loss on
     * large or high-scale values. compareTo() ignores scale, so "1.50" equals "1.5".
     */
    private boolean compareNumbers(String str1, String str2, String op) {
        BigDecimal num1 = new BigDecimal(str1);
        BigDecimal num2 = new BigDecimal(str2);
        int cmp = num1.compareTo(num2);

        switch (op) {
            case OP_EQ:
                return cmp == 0;
            case OP_NEQ:
                return cmp != 0;
            case OP_LT:
            case OP_LT_ALIAS:
                return cmp < 0;
            case OP_GT:
            case OP_GT_ALIAS:
                return cmp > 0;
            case OP_LTE:
                return cmp <= 0;
            case OP_GTE:
                return cmp >= 0;
            default:
                // Unreachable: operator is validated in execute().
                throw new IllegalStateException("Unhandled numeric operator: " + op);
        }
    }

    private boolean compareStrings(String str1, String str2, String op) {
        switch (op) {
            case OP_EQ:
                return str1.equals(str2);
            case OP_NEQ:
                return !str1.equals(str2);
            case OP_LT:
            case OP_LT_ALIAS:
                return str1.compareTo(str2) < 0;
            case OP_GT:
            case OP_GT_ALIAS:
                return str1.compareTo(str2) > 0;
            case OP_LTE:
                return str1.compareTo(str2) <= 0;
            case OP_GTE:
                return str1.compareTo(str2) >= 0;
            case OP_CONTAINS:
                return str1.contains(str2);
            case OP_NOT_CONTAINS:
                return !str1.contains(str2);
            default:
                // Unreachable: operator is validated in execute().
                throw new IllegalStateException("Unhandled string operator: " + op);
        }
    }
}