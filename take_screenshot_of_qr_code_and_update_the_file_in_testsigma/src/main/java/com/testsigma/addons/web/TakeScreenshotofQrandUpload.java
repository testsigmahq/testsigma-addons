package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.openqa.selenium.OutputType;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

@Data
@Action(actionText = "Take screenshot of the UI-Component: element and upload it on testsigma uploads with Project_ID: projectid ,Application-ID: applicationid, Upload-Name: uploadname, Upload-EndPoint: url,API-Key: APIKEY",
        description = "Take Screenshot of elemet and updates it on testsigma uploads",
        applicationType = ApplicationType.WEB)
public class TakeScreenshotofQrandUpload extends WebAction {

	
  @Element(reference = "element")
  private com.testsigma.sdk.Element element;
@TestData(reference = "projectid")
  private com.testsigma.sdk.TestData projectid;
  @TestData(reference = "applicationid")
  private com.testsigma.sdk.TestData applicationid;
  @TestData(reference = "uploadname")
  private com.testsigma.sdk.TestData uploadname;
  @TestData(reference = "url")
  private com.testsigma.sdk.TestData url;
  @TestData(reference = "APIKEY")
  private com.testsigma.sdk.TestData APIKEY;
  
 

  @Override
  public com.testsigma.sdk.Result execute()  {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    
    try {
    	
    	File file =element.getElement().getScreenshotAs(OutputType.FILE);
       
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        long timeMilli = date.getTime();
        long timeMilli2 = calendar.getTimeInMillis();
        String version = String.valueOf(timeMilli+timeMilli2);
        
        logger.info(version + "File path ========"+file.getAbsolutePath());
        
        OkHttpClient client = new OkHttpClient().newBuilder()
    			  .build();
    			MediaType mediaType = MediaType.parse("text/plain");
    			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
    			  .addFormDataPart("fileContent",file.getAbsolutePath(),
    			    RequestBody.create(MediaType.parse("application/octet-stream"),
    			    new File(file.getAbsolutePath())))
    			  .addFormDataPart("projectId",projectid.getValue().toString())
    			  .addFormDataPart("name",uploadname.getValue().toString())
    			  .addFormDataPart("uploadType","Attachment")
    			  .addFormDataPart("platformType","TestsigmaLab")
    			  .addFormDataPart("isPublic","true")
    			  .addFormDataPart("applicationId",applicationid.getValue().toString())
    			  .addFormDataPart("Version",version)
    			  .build();
    			Request request = new Request.Builder()
    			  .url(url.getValue().toString())
    			  .method("PUT", body)
    			  .addHeader("Authorization",APIKEY.getValue().toString())
    			  .build();
    			Response response = client.newCall(request).execute();
    			
    			
        setSuccessMessage("Successfully uploaded the QR code in Testsigma Upoload"+response.body().string()+ " Response code is "+response.code());
    	
    }catch(Exception e){
    	
    	logger.debug(e.getMessage());
    	setErrorMessage("Operation failed "+e.getMessage());
    	
    }

  
    return result;
  }}
