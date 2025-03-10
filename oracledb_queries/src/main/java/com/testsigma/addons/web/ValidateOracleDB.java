package com.testsigma.addons.web;

import com.google.common.io.Files;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.DriverAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import dbMethods.DBConnection;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Data
@Action(actionText = "Execute query Select-Query in the database with connection Connection-name and wallet config Config-Path using username Username and password Password " +
        "and store the results into run time variable Output-Variable",
        description = "Execute select query in oracle database",
        applicationType = ApplicationType.WEB,
        actionType = StepActionType.NONE)

public class ValidateOracleDB extends DriverAction {
    @TestData(reference =  "Connection-name")
    private com.testsigma.sdk.TestData connectionName;

    @TestData(reference =  "Config-Path")
    private com.testsigma.sdk.TestData configPath;

    @TestData(reference =  "Username")
    private com.testsigma.sdk.TestData username;

    @TestData(reference =  "Password")
    private com.testsigma.sdk.TestData password;

    @TestData(reference = "Select-Query")
    private com.testsigma.sdk.TestData query;

    @TestData(reference = "Output-Variable")
    private com.testsigma.sdk.TestData queryOutput;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {

        Result result = Result.SUCCESS;

        String db_name = connectionName.getValue().toString();
        String selectQuery = query.getValue().toString();
        String uname = username.getValue().toString();
        String password = this.password.getValue().toString();

        try {
            String wallet_path = getWalletPath();
            DBConnection connection = new DBConnection();
            JSONArray query_result = connection.executeQueries(db_name, uname, password, selectQuery, wallet_path);
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(queryOutput.getValue().toString());
            runTimeData.setValue(query_result.toString());
            logger.info(query_result.toString());
            setSuccessMessage("The mentioned query is executed successfully and the results are stored in run time variable. You can also see the results under AddonNLP logs.");
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Please verify the details entered i.e. query, username, password are correct:"+e.getMessage());
            result = Result.FAILED;
            return result;
        }
        return result;
    }

    public String getWalletPath() throws Exception {
        File sourceFile = null;
        File destinationFile = Files.createTempDir();
        boolean isZipFile = true;
        if(configPath.getValue().toString().toUpperCase().startsWith("HTTP")){
            //Download from aws S3 presigned URL
            sourceFile = File.createTempFile("wallet",".zip");
            File.createTempFile("wallet",".zip");
            URL url = new URL(configPath.getValue().toString());
            FileUtils.copyURLToFile(url,sourceFile);
        }else{
            sourceFile = new File(configPath.getValue().toString());
            if(!sourceFile.getName().toUpperCase().trim().endsWith(".ZIP")){
                return configPath.getValue().toString().trim();
            }
        }
        //Extract Zip file

        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis= new ZipInputStream(new FileInputStream(sourceFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while(zipEntry !=null) {
                String filePath = destinationFile + File.separator + zipEntry.getName();
                if(!zipEntry.isDirectory()) {
                    FileOutputStream fos = new FileOutputStream(filePath);
                    int len;
                    while ((len = zis.read(buffer)) >0){
                        fos.write(buffer,0,len);
                    }
                    fos.close();
                }
                else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            throw e;
        }
        return destinationFile.getAbsolutePath().toString();
    }
}

