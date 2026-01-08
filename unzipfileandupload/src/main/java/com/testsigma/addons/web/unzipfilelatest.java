package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;

import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Data
@Action(actionText = "Unzip the latest zipfile filedirectory to destination destfilepath and store the filepath into a variable testdata",
description = "Unzip the file and upload into a element",
applicationType = ApplicationType.WEB,
useCustomScreenshot = false)
public class unzipfilelatest extends WebAction {

	@TestData(reference = "filedirectory")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "destfilepath")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "testdata" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData3;
	
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;


	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		//Your Awesome code starts here
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		try {
			File zipFilePath = getLastModified(testData1.getValue().toString());
			String destDir = testData2.getValue().toString();

			String fileNameupload = unzip(zipFilePath, destDir);
			String filePath = destDir + File.separator + fileNameupload;
			runTimeData.setValue(filePath);
	        runTimeData.setKey(testData3.getValue().toString());
			Thread.sleep(1000);
			setSuccessMessage("File was unzipped and stored successfully in : " + testData3.getValue().toString() + " and the filepath is :" + filePath);
		}catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		}
		return result;		
	}
	private static String unzip(File zipFilePath, String destDir) throws InterruptedException {
		String fileName = null;
		Thread.sleep(1000);
		File dir = new File(destDir);
		// create output directory if it doesn't exist
		if (!dir.exists()) dir.mkdirs();
		FileInputStream fis;
		// buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(zipFilePath);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				fileName = ze.getName();
				File newFile = new File(destDir + File.separator + fileName);
				// create directories for sub directories in zip
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				Thread.sleep(1000);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zis.closeEntry();
				ze = zis.getNextEntry();
				Thread.sleep(1000);
			}
			zis.closeEntry();
			zis.close();
			fis.close();
			Thread.sleep(1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName;
	}
	
	public static File getLastModified(String directoryFilePath)
	{
		File directory = new File(directoryFilePath);
		File[] files = directory.listFiles(File::isFile);
		long lastModifiedTime = Long.MIN_VALUE;
		File chosenFile = null;

		if (files != null)
		{
			for (File file : files)
			{
				if (file.lastModified() > lastModifiedTime)
				{
					chosenFile = file;
					lastModifiedTime = file.lastModified();
				}
			}
		}

		return chosenFile;
	}
}