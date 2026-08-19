package com.testsigma.addons.windows;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

@Data
@Action(
        actionText = "Round testdata to decimalplaces decimal places and store in runtimevariable",
        description = "Round the test data (plain, Western \",\" grouped e.g. 1,000,000.00, or Indian \",\" grouped e.g. 1,00,00,000.00) to the specified number of decimal places, keeping the same grouping style in the output, and store the result in a runtime variable",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = false
)
public class RoundGroupedDecimalValues extends WindowsAction {

    private static final String PLAIN = "[+-]?\\d+(\\.\\d+)?";
    private static final String WESTERN_GROUPED = "[+-]?\\d{1,3}(,\\d{3})*(\\.\\d+)?";
    private static final String INDIAN_GROUPED = "[+-]?\\d{1,2}(,\\d{2})+,\\d{3}(\\.\\d+)?";

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "decimalplaces")
    private com.testsigma.sdk.TestData decimalPlaces;
    @TestData(reference = "runtimevariable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runTimeVar;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating comma grouped decimal rounding");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            // Get the input value
            String inputValue = testData.getValue().toString().trim();
            // Validate input value
            if (inputValue.isEmpty()) {
                setErrorMessage("Input value cannot be empty");
                return Result.FAILED;
            }

            // Detect grouping style, rejecting anything that matches none of them
            boolean isIndian = inputValue.matches(INDIAN_GROUPED);
            boolean isWestern = !isIndian && inputValue.matches(WESTERN_GROUPED) && inputValue.contains(",");
            boolean isPlain = !isIndian && !isWestern && inputValue.matches(PLAIN);
            if (!isIndian && !isWestern && !isPlain) {
                setErrorMessage("Invalid number format: " + inputValue);
                return Result.FAILED;
            }

            // Get the number of decimal places
            int scale = Integer.parseInt(decimalPlaces.getValue().toString().trim());

            // Validate decimal places
            if (scale < 0) {
                setErrorMessage("Decimal places must be a positive integer");
                return Result.FAILED;
            }

            logger.debug("Input value: " + inputValue);
            logger.debug("Decimal places: " + scale);
            logger.debug("Runtime variable: " + runTimeVar.getValue().toString().trim());

            // Remove grouping separators and round
            String cleanValue = inputValue.replace(",", "");
            BigDecimal number = new BigDecimal(cleanValue);
            BigDecimal roundedNumber = number.setScale(scale, RoundingMode.HALF_UP);

            // Split into integer / fractional parts to reapply grouping to the integer part only
            String[] parts = roundedNumber.toPlainString().split("\\.");
            String intPart = parts[0];
            String fracPart = parts.length > 1 ? parts[1] : "";

            String formattedInt;
            if (isIndian) {
                formattedInt = groupIndian(intPart);
            } else if (isWestern) {
                formattedInt = new DecimalFormat("#,##0").format(new BigDecimal(intPart));
            } else {
                formattedInt = intPart;
            }

            String roundedValue = scale > 0 ? formattedInt + "." + fracPart : formattedInt;

            runTimeData.setKey(runTimeVar.getValue().toString().trim());
            runTimeData.setValue(roundedValue);
            logger.info("Value rounded successfully. " + inputValue + " -> " + roundedValue);
            setSuccessMessage("Value rounded successfully and stored in runtime variable: " + runTimeVar.getValue().toString().trim() + " = " + roundedValue);
        } catch (Exception error) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Rounding decimal value failed: " + ExceptionUtils.getStackTrace(error));
            setErrorMessage("Rounding decimal value failed: " + ExceptionUtils.getMessage(error));
        }
        return result;
    }

    // Java's DecimalFormat has no built-in support for Indian (2-2-3) digit grouping,
    // so the last-3-then-groups-of-2 pattern is built manually here.
    private static String groupIndian(String digits) {
        boolean negative = digits.startsWith("-");
        if (negative) {
            digits = digits.substring(1);
        }
        if (digits.length() <= 3) {
            return (negative ? "-" : "") + digits;
        }
        String lastThree = digits.substring(digits.length() - 3);
        String remaining = digits.substring(0, digits.length() - 3);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = remaining.length() - 1; i >= 0; i--) {
            sb.append(remaining.charAt(i));
            count++;
            if (count % 2 == 0 && i != 0) {
                sb.append(',');
            }
        }
        return (negative ? "-" : "") + sb.reverse().toString() + "," + lastThree;
    }
}
