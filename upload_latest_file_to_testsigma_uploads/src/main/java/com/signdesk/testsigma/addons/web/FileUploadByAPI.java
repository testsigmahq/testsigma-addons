package com.signdesk.testsigma.addons.web;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.signdesk.testsigma.addons.utils.TestsigmaUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import okhttp3.Response;

@Data
@Action(actionText = "Upload file with path on testsigma uploads with projectid,applicationid,uploadname,url with Testsigma API key api_key",
        description = "Uploads the latest file from the given path to Testsigma Uploads using the Testsigma API. Requires project ID, application ID, upload name, API URL, and API key.",
        applicationType = ApplicationType.WEB)
public class FileUploadByAPI extends WebAction {

	@TestData(reference = "path")
	private com.testsigma.sdk.TestData path;
	@TestData(reference = "projectid")
	private com.testsigma.sdk.TestData projectid;
	@TestData(reference = "applicationid")
	private com.testsigma.sdk.TestData applicationid;
	@TestData(reference = "uploadname")
	private com.testsigma.sdk.TestData uploadname;
	@TestData(reference = "url")
	private com.testsigma.sdk.TestData url;
	@TestData(reference = "api_key")
	private com.testsigma.sdk.TestData APIKEY;

	@Override
	public com.testsigma.sdk.Result execute() {
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		try (Response response = TestsigmaUtils.uploadFile(
				path.getValue().toString(),
				projectid.getValue().toString(),
				applicationid.getValue().toString(),
				uploadname.getValue().toString(),
				url.getValue().toString(),
				APIKEY.getValue().toString())) {

			if (!response.isSuccessful()) {
				throw new RuntimeException("Unexpected response code: " + response);
			}
			setSuccessMessage("Successfully uploaded file to testsigma uploads. Response code is " + response.code());

		} catch (Exception e) {
			result = Result.FAILED;
			logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
			setErrorMessage("Exception Occurred: " + ExceptionUtils.getMessage(e));
		}

		return result;
	}
}
