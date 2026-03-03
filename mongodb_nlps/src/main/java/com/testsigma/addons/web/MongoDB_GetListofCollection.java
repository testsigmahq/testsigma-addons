package com.testsigma.addons.web;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Get the list of collections on the DBname and MongoDB_Connection",
description = "validates list of collections fetched from db",
applicationType = ApplicationType.WEB)
public class MongoDB_GetListofCollection extends WebAction {

	@TestData(reference = "DBname")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "MongoDB_Connection")
	private com.testsigma.sdk.TestData testData2;
	StringBuffer sb = new StringBuffer();

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
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
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}