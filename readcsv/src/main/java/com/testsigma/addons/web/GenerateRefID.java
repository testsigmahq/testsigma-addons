package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;

@Data
@Action(actionText = "Generate testdata number of RefId Csv with name testdata2 and store it in downloads",
        description = "Generates random csv file and stores the newly created csv file in a directory",
        applicationType = ApplicationType.WEB)
public class GenerateRefID extends WebAction {


    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData numberofImei;

    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData csvname;


    @Override
    public Result execute() throws NoSuchElementException {

        logger.info("Initiating execution");

        Result result = Result.SUCCESS;
        Random random = new Random();
        try{
            String staticValue = "RQAEZ";
            int numOfIMEIs = Integer.valueOf(numberofImei.getValue().toString());
            String[] imeis = new String[numOfIMEIs];

            //String[] randomStrings = new String[numOfIMEIs];
            for (int i = 0; i < numOfIMEIs; i++) {
                String randomNum = String.format("%05d", random.nextInt(100000));
                imeis[i] = staticValue + randomNum;
            }

            // Create a new CSV file with header "IMEI" and write the IMEI values to it
            String csvFile = System.getProperty("user.home") +File.separator+ "Downloads" +File.separator+ csvname.getValue().toString() + ".csv";
            File file = new File(csvFile);
            String absolutePath = file.getAbsolutePath();
            logger.info(absolutePath);
            String csvHeader = "RefNo";
            try (FileWriter writer = new FileWriter(csvFile)) {
                writer.append(csvHeader);
                writer.append("\n");

                for (String imei : imeis) {
                    writer.append(imei);
                    writer.append("\n");
                }

                writer.flush();
            }
            setSuccessMessage("RefIds generated and saved to file: "+csvFile);

        }catch (Exception e){
            result = Result.FAILED;
            setErrorMessage("Operation failed "+e.getMessage());

        }
        return result;

    }}



