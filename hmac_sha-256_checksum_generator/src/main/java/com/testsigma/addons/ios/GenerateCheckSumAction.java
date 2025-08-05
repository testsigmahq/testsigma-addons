package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Data
@Action(
    actionText = "Generate HMAC SHA-256 checksum using secret key secret-key and JSON string json-string and store it into a runtime variable variable-name",
    description = "Generates HMAC SHA-256 checksum and stores into runtime variable, where JsonString format for e.g.({\"status\":\"SUCCESS\",\"transactionReference\":\"tr13\",\"payeeVpa\":\"xyz\",\"payerVpa\":\"xyz\",\"rrn\":\"xyz\",\"txnTime\":\"2025-05-07T09:31:22.345+05:30\",\"payerAccountType\":\"xyz\",\"amount\":\"xyz\",\"paymentInitMode\":\"01\",\"status_id\":\"2\"}}\",",
    applicationType = ApplicationType.IOS,
    useCustomScreenshot = false
)

public class GenerateCheckSumAction extends IOSAction {

    @TestData(reference = "secret-key")
    private com.testsigma.sdk.TestData secretKey;

    @TestData(reference = "json-string")
    private com.testsigma.sdk.TestData jsonString;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Execution started for checksum generation");

        try {
            logger.debug("Input secret key: " + secretKey.getValue());
            logger.debug("Input JSON string: " + jsonString.getValue());

            String checksum = createHmacChecksum(jsonString.getValue().toString(), secretKey.getValue().toString());
            logger.info("Checksum generated successfully: " + checksum);

            // Store into runtime variable
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(checksum);
            logger.info("Checksum stored in runtime variable: " + variableName.getValue());

            setSuccessMessage("Checksum successfully generated. Checksum: " + checksum);
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Error generating checksum: "+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to generate checksum: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

    public String createHmacChecksum(String data, String key) throws Exception {
        logger.debug("Starting HMAC-SHA256 checksum generation.");
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKeySpec);
        byte[] hmacBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        logger.debug("HMAC-SHA256 byte array generated.");
        return bytesToHex(hmacBytes);
    }

    private String bytesToHex(byte[] bytes) {
        logger.debug("Converting byte array to hex string.");
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        logger.debug("Hex string conversion completed.");
        return hexString.toString();
    }
}
