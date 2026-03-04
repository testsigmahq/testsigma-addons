package com.testsigma.addons.android;

import com.mongodb.*;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Iterator;

@Data
@Action(actionText = "Get the values of particular collection CollectionName on Database DBname and connection MongoDB_ConnectionURL",
description = "validates values of particular collection fetched from db with the connection localhost:port/username/password",
applicationType = ApplicationType.ANDROID)
public class MongoDB_GetvaluesofCollectionWithConnectionURL extends AndroidAction {

	@TestData(reference = "DBname")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "MongoDB_ConnectionURL")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "CollectionName")
	private com.testsigma.sdk.TestData testData3;
	StringBuffer sb = new StringBuffer();

	@Override
	public Result execute() throws NoSuchElementException {
		Result result = Result.SUCCESS;
		logger.info("Initiating execution");
		try{
			String database = testData1.getValue().toString();
			String connection = testData2.getValue().toString();

			MongoClientURI uri = new MongoClientURI(connection);
			MongoClient mongoClient = new MongoClient(uri);
			
			DB db = mongoClient.getDB(database);

			DBCollection collection = db.getCollection(testData3.getValue().toString());

			DBCursor iterDoc = collection.find();
			Iterator it = iterDoc.iterator();
			sb.append("<br>");
			while (it.hasNext()) {
				sb.append(it.next());
				sb.append("<br>");
			}
			setSuccessMessage("Successfully fetched the values from collection: " +sb.toString());
			logger.info("Successfully fetched the values from collection: " +sb.toString());
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