package com.testsigma.addons.hook;

import com.testsigma.addons.hook.utility.ApiMethods;
import com.testsigma.addons.hook.utility.FileMethods;
import com.testsigma.sdk.Hook;
import com.testsigma.sdk.HookType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Email;
import com.testsigma.sdk.annotation.RunResult;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestPlanHook;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@TestPlanHook(
        name = "Send TestData Profile Data as Excel To Mail",
        type = HookType.AFTER
)
public class SendTDPToMailHook extends Hook {

    private static final String testDataEndpoint = "https://app.testsigma.com/api/v1/test_data/";

    @TestData(reference = "{To Email Ids (Comma Separated)}")
    private com.testsigma.sdk.TestData emailIds;

    @TestData(reference = "{Email Subject}")
    private com.testsigma.sdk.TestData emailSubject;

    @TestData(reference = "{Email Body}")
    private com.testsigma.sdk.TestData emailBody;

    @TestData(reference = "{Test Data Profile Ids (Comma Separated)}")
    private com.testsigma.sdk.TestData tdpIds;

    @TestData(reference = "{Testsigma API Key}")
    private com.testsigma.sdk.TestData apikey;

    @Email
    private com.testsigma.sdk.Email email;

    @RunResult
    private com.testsigma.sdk.RunResult runResult;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;

        ApiMethods apiMethods = new ApiMethods();
        FileMethods write = new FileMethods();

        String apiKeyValue = apikey.getValue().toString();
        String[] tdpIdsValue = tdpIds.getValue().toString().split(",");

        String[] recipients = emailIds.getValue().toString().split(",");
        String emailSubjectValue = emailSubject.getValue().toString();
        String emailBodyValue = emailBody.getValue().toString();

        List<File> destination_files = new ArrayList<>();

        try {
            for (int i = 0; i < tdpIdsValue.length; i++) {
                String tdpId = tdpIdsValue[i].trim();
                String testDataResponse = apiMethods.makeGetRequest(testDataEndpoint, tdpId, apiKeyValue);

                JSONObject obj = new JSONObject(testDataResponse);
                String tdpName = obj.getString("testDataName");
                // Remove spaces and special chars from tdpName just to be safe for a filename
                String safeTdpName = tdpName.replaceAll("[^a-zA-Z0-9.-]", "_");

                File file = new File(System.getProperty("java.io.tmpdir"), safeTdpName + "_" + tdpId + ".xlsx");
                file.deleteOnExit();

                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    write.writeToExcelFile(file, testDataResponse, workbook);
                }
                destination_files.add(file);
            }

            email.setTo(Arrays.asList(recipients));
            email.setSubject(emailSubjectValue + " For the RunResult Id: " + runResult.getId());
            email.setBody(emailBodyValue);
            email.setAttachments(destination_files);
            Boolean mailSent = email.send();
            logger.info("Mail sent status - " + mailSent);
            setSuccessMessage("The specified test data profiles have been extracted and sent to the respective emails.");

        } catch (Exception e) {
            logger.info("Error in sending email: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error in sending email: " +  ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }
        return result;
    }
}
