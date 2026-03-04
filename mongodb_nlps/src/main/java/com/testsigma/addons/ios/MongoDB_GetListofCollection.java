package com.testsigma.addons.ios;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Set;

@Data
@Action(actionText = "Get the list of collections on the DBname and MongoDB_Connection",
description = "validates list of collections fetched from db",
applicationType = ApplicationType.IOS)
public class MongoDB_GetListofCollection extends IOSAction {

	@TestData(reference = "DBname")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "MongoDB_Connection")
	private com.testsigma.sdk.TestData testData2;
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		try{
			String connection = testData2.getValue().toString(); 
			
			MongoClientURI uri = new MongoClientURI(connection);
			MongoClient mongoClient = new MongoClient(uri);

			DB db = mongoClient.getDB(testData1.getValue().toString());

			Set<String> colls = db.getCollectionNames();
			
			sb.append("<br>");
			for (String collections : colls) {
				sb.append(collections);
			}
			setSuccessMessage("Successfully fetched the collection list: " +sb.toString());
			logger.info("Successfully fetched the collection list: " +sb.toString() +connection);
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