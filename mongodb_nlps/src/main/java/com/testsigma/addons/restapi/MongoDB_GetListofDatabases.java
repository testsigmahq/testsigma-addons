package com.testsigma.addons.restapi;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Get the list of databases on the MongoDB_Connection",
description = "validates list of databases fetched from db",
applicationType = ApplicationType.REST_API)
public class MongoDB_GetListofDatabases extends RestApiAction {

	@TestData(reference = "MongoDB_Connection")
	private com.testsigma.sdk.TestData testData1;
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		try{
			String connection = testData1.getValue().toString();

			MongoClientURI uri = new MongoClientURI(connection);
			MongoClient mongoClient = new MongoClient(uri);
			
			MongoCursor<String> dbsCursord = mongoClient.listDatabaseNames().iterator();
			
			sb.append("<br>");
			while(dbsCursord.hasNext()) {
			      sb.append(dbsCursord.next());
			      sb.append("<br>");
			 }
			setSuccessMessage("Successfully fetched the databases list: " +sb.toString());
			logger.info("Successfully fetched the databases list: " +sb.toString());
		}
		catch (Exception e){
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}