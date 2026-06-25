package com.testsigma.addons.salesforce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.SalesforceAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Action(actionText = "Copy reference TDP reference-tdp-id to target TDP target-tdp-id updating column at index" +
        " column-index in set set-name with value new-value using api key api-key and store resolved column name" +
        " in column-name-variable",
        description = "Fetches the reference TDP in full, updates only the specified column (by 1-based index)" +
                " in the target row, then PUTs the entire modified payload to the target TDP — making it an exact" +
                " replica of the reference with one value changed. Ignores whatever was previously in the target TDP.",
        applicationType = ApplicationType.Salesforce,
        useCustomScreenshot = false)
public class UpdateTDPColumnByIndex extends SalesforceAction {

    @TestData(reference = "reference-tdp-id")
    private com.testsigma.sdk.TestData referenceTdpId;

    @TestData(reference = "target-tdp-id")
    private com.testsigma.sdk.TestData targetTdpId;

    @TestData(reference = "set-name")
    private com.testsigma.sdk.TestData setName;

    @TestData(reference = "column-index")
    private com.testsigma.sdk.TestData columnIndex;

    @TestData(reference = "new-value")
    private com.testsigma.sdk.TestData newValue;

    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData apiKey;

    @TestData(reference = "column-name-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData columnNameVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating UpdateTDPColumnByIndex execution");

        String refTdpIdStr = referenceTdpId.getValue().toString().trim();
        String tgtTdpIdStr = targetTdpId.getValue().toString().trim();
        String setNameStr = setName.getValue().toString().trim();
        String columnIndexStr = columnIndex.getValue().toString().trim();
        String newValueStr = newValue.getValue().toString().trim();
        String apiKeyStr = apiKey.getValue().toString().trim();
        String columnNameVar = columnNameVariable.getValue().toString().trim();

        logger.info("Reference TDP ID: " + refTdpIdStr + ", Target TDP ID: " + tgtTdpIdStr
                + ", Set Name: " + setNameStr + ", Column Index: " + columnIndexStr + ", New Value: " + newValueStr);

        int requestedIndex;
        try {
            requestedIndex = Integer.parseInt(columnIndexStr);
            if (requestedIndex < 1) {
                setErrorMessage("Column index must be 1 or greater, but got: " + columnIndexStr);
                return Result.FAILED;
            }
        } catch (NumberFormatException e) {
            setErrorMessage("Column index must be a valid integer, but got: " + columnIndexStr);
            return Result.FAILED;
        }

        try {
            String refUrl = "https://app.testsigma.com/api/v1/test_data/" + refTdpIdStr;
            logger.info("Fetching reference TDP from: " + refUrl);
            String responseBody = TDPApiUtil.makeHttpRequest2(refUrl, apiKeyStr, logger);
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode columnsNode = root.get("columns");
            if (columnsNode == null || !columnsNode.isArray() || columnsNode.size() == 0) {
                setErrorMessage("No columns array found in reference TDP " + refTdpIdStr);
                return Result.FAILED;
            }

            List<String> dataColumns = new ArrayList<>();
            for (JsonNode col : columnsNode) {
                String name = col.asText();
                if (!name.equals("S.No.") && !name.equals("ETF") && !name.equals("Set Name")) {
                    dataColumns.add(name);
                }
            }
            logger.info("Data columns in reference TDP (" + dataColumns.size() + "): " + dataColumns);

            if (requestedIndex > dataColumns.size()) {
                setErrorMessage("Column index " + requestedIndex + " exceeds available data columns ("
                        + dataColumns.size() + "). Available columns: " + dataColumns);
                return Result.FAILED;
            }

            String resolvedColumnName = dataColumns.get(requestedIndex - 1);
            logger.info("Resolved column at index " + requestedIndex + ": " + resolvedColumnName);

            JsonNode dataArrayNode = root.get("data");
            if (dataArrayNode == null || !dataArrayNode.isArray()) {
                setErrorMessage("No data array found in reference TDP " + refTdpIdStr);
                return Result.FAILED;
            }
            ArrayNode dataArray = (ArrayNode) dataArrayNode;

            boolean rowFound = false;
            for (JsonNode row : dataArray) {
                if (setNameStr.equals(row.get("name").asText())) {
                    JsonNode rowData = row.get("data");
                    if (rowData != null && rowData.isObject()) {
                        ((ObjectNode) rowData).put(resolvedColumnName, newValueStr);
                    }
                    rowFound = true;
                    break;
                }
            }

            if (!rowFound) {
                setErrorMessage("Row with name '" + setNameStr + "' not found in reference TDP " + refTdpIdStr);
                return Result.FAILED;
            }

            ObjectNode putBody = objectMapper.createObjectNode();
            putBody.put("testDataName", root.get("testDataName").asText());
            putBody.set("data", dataArray);
            putBody.set("columns", columnsNode);

            String tgtUrl = "https://app.testsigma.com/api/v1/test_data/" + tgtTdpIdStr;
            logger.info("Putting modified reference payload to target TDP: " + tgtUrl);
            TDPApiUtil.makeHttpRequestWithBody(tgtUrl, "PUT", objectMapper.writeValueAsString(putBody), apiKeyStr, logger);
            logger.info("Target TDP successfully replaced with modified reference data");

            runTimeData.setKey(columnNameVar);
            runTimeData.setValue(resolvedColumnName);
            logger.info("Stored resolved column name '" + resolvedColumnName + "' in runtime variable: " + columnNameVar);

            setSuccessMessage("Successfully copied reference TDP <b>" + refTdpIdStr + "</b> to target TDP <b>"
                    + tgtTdpIdStr + "</b> with column <b>" + resolvedColumnName + "</b> (index " + requestedIndex
                    + ") set to <b>" + newValueStr + "</b> in set <b>" + setNameStr + "</b>");
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            logger.info("Error occurred while updating TDP column by index: " + ExceptionUtils.getMessage(e));
            setErrorMessage("Failed to update TDP column by index: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }
}
