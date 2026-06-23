package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(
        actionText = "Create or update row row-name in test data profile test-data-id with columns-data (Eg:{\"Column1\": \"value1\", \"Column2\": \"value2\"}  using api key api-key",
        description = "Creates or updates a row in a Testsigma Test Data Profile using a JSON object of column-value pairs. New columns are also created automatically if they do not already exist in the profile.",
        applicationType = ApplicationType.WEB
)
public class TdpRowColumnCreatorUpdaterAction extends WebAction {

    private static final String TESTSIGMA_API_BASE_URL = "https://app.testsigma.com/api/v1/test_data/";

    @TestData(reference = "test-data-id")
    private com.testsigma.sdk.TestData testDataId;

    @TestData(reference = "row-name")
    private com.testsigma.sdk.TestData rowName;

    // JSON string: {"Column1": "value1", "Column2": "value2"}
    @TestData(reference = "columns-data")
    private com.testsigma.sdk.TestData columnsData;

    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData apiKey;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            String tdpId  = testDataId.getValue().toString().trim();
            String row    = rowName.getValue().toString().trim();
            String rawJson = columnsData.getValue().toString().trim();
            String token  = apiKey.getValue().toString().trim();
            String apiUrl = TESTSIGMA_API_BASE_URL + tdpId;

            logger.info("===== INPUT =====");
            logger.info("TDP ID: " + tdpId);
            logger.info("Row: " + row);
            logger.info("Columns Data: " + rawJson);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode inputData = mapper.readTree(rawJson);
            if (!inputData.isObject()) {
                setErrorMessage("columns-data must be a JSON object, e.g. {\"Column1\": \"value1\"}");
                return Result.FAILED;
            }

            // ===== GET =====
            logger.info("\n===== GET REQUEST =====");
            Response getResponse = RestAssured.given()
                    .baseUri(apiUrl)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .get();

            logger.info("GET Status: " + getResponse.getStatusCode());
            logger.info("GET Body: " + getResponse.getBody().asString());

            if (getResponse.getStatusCode() != 200) {
                setErrorMessage("GET failed: " + getResponse.getBody().asString());
                return Result.FAILED;
            }

            JsonNode root = mapper.readTree(getResponse.getBody().asString());
            String testDataName = root.path("testDataName").asText();

            // ===== Columns: merge existing + any new from input =====
            ArrayNode existingColumns = (ArrayNode) root.path("columns");
            List<String> columnsList = new ArrayList<>();
            for (JsonNode col : existingColumns) {
                columnsList.add(col.asText());
            }
            for (Iterator<String> it = inputData.fieldNames(); it.hasNext(); ) {
                String colName = it.next();
                if (!columnsList.contains(colName)) {
                    logger.info("Adding new column: " + colName);
                    columnsList.add(colName);
                }
            }

            // ===== Rows =====
            ArrayNode existingData = (ArrayNode) root.path("data");
            ArrayNode updatedData  = mapper.createArrayNode();
            boolean rowFound = false;

            for (JsonNode rowNode : existingData) {
                ObjectNode updatedRow = mapper.createObjectNode();
                if (rowNode.has("id")) {
                    updatedRow.put("id", rowNode.get("id").asLong());
                }

                String currentRowName = rowNode.get("name").asText();
                updatedRow.put("name", currentRowName);

                ObjectNode rowData = mapper.createObjectNode();
                rowNode.get("data").fields().forEachRemaining(e ->
                        rowData.put(e.getKey(), e.getValue().asText())
                );

                if (currentRowName.equals(row)) {
                    logger.info("Updating existing row: " + row);
                    inputData.fields().forEachRemaining(e -> rowData.put(e.getKey(), e.getValue().asText()));
                    rowFound = true;
                } else {
                    // fill any newly added columns with empty string for other rows
                    inputData.fieldNames().forEachRemaining(colName -> {
                        if (!rowData.has(colName)) rowData.put(colName, "");
                    });
                }

                updatedRow.set("data", rowData);
                updatedData.add(updatedRow);
            }

            if (!rowFound) {
                logger.info("Row not found, creating new row: " + row);
                ObjectNode newRow = mapper.createObjectNode();
                newRow.put("name", row);
                ObjectNode newRowData = mapper.createObjectNode();
                for (String col : columnsList) {
                    JsonNode inputVal = inputData.get(col);
                    newRowData.put(col, inputVal != null ? inputVal.asText() : "");
                }
                newRow.set("data", newRowData);
                updatedData.add(newRow);
            }

            // ===== Payload =====
            ObjectNode payload = mapper.createObjectNode();
            payload.put("testDataName", testDataName);
            payload.set("data", updatedData);
            ArrayNode columnsArray = mapper.createArrayNode();
            columnsList.forEach(columnsArray::add);
            payload.set("columns", columnsArray);

            String payloadJson = mapper.writeValueAsString(payload);
            logger.info("\n===== PUT PAYLOAD =====");
            logger.info(payloadJson);

            // ===== PUT =====
            logger.info("\n===== PUT REQUEST =====");
            Response putResponse = RestAssured.given()
                    .baseUri(apiUrl)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(payloadJson)
                    .put();

            logger.info("PUT Status: " + putResponse.getStatusCode());
            logger.info("PUT Body: " + putResponse.getBody().asString());

            if (putResponse.getStatusCode() < 200 || putResponse.getStatusCode() >= 300) {
                setErrorMessage("PUT failed: " + putResponse.getBody().asString());
                return Result.FAILED;
            }

            String successMsg = "SUCCESS: Updated row '" + row + "' with " + rawJson;
            logger.info(successMsg);
            setSuccessMessage(successMsg);
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getMessage(e));
            setErrorMessage("Exception: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }
}