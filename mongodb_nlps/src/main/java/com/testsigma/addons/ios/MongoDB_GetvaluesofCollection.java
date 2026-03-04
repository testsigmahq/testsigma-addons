package com.testsigma.addons.ios;

import com.mongodb.*;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Iterator;

@Data
@Action(actionText = "Get the values of particular collection CollectionName on the DBname and MongoDB_Connection",
description = "validates values of particular collection fetched from db",
applicationType = ApplicationType.IOS)
public class MongoDB_GetvaluesofCollection extends IOSAction {

	@TestData(reference = "DBname")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "MongoDB_Connection")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "CollectionName")
	private com.testsigma.sdk.TestData testData3;
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