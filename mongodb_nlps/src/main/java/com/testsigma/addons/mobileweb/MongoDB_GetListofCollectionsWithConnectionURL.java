package com.testsigma.addons.mobileweb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Set;

@SuppressWarnings("deprecation")
@Data
@Action(actionText = "Get the list of collections on DBname and connection MongoDB_ConnectionURL",
description = "validates values of particular collection fetched from db with connectionURL localhost:port/username/password",
applicationType = ApplicationType.MOBILE_WEB)
public class MongoDB_GetListofCollectionsWithConnectionURL extends WebAction {

	@TestData(reference = "DBname")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "MongoDB_ConnectionURL")
	private com.testsigma.sdk.TestData testData2;
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

			Set<String> colls = mongoClient.getDB(database).getCollectionNames();
			
			sb.append("<br>");
			for (String collections : colls) {
				sb.append(collections);
				sb.append("<br>");
			}
			setSuccessMessage("Successfully fetched the collection list:  " +sb.toString());
			logger.info("Successfully fetched the collection list: " +sb.toString());
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