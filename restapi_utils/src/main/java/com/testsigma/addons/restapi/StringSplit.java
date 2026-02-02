package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Split String 'String1' by Delimiter 'Comma', Retrieve 'Pos' Position from Array,store in RunTime 'StringSplit' (TG)",
        description = "Spit string by default comma (,) and store value of string based on position into runtime variable StringSplit",
        applicationType = ApplicationType.REST_API)
public class StringSplit extends RestApiAction {

    @TestData(reference = "String1")
    private com.testsigma.sdk.TestData testDataString1;
    @TestData(reference = "Comma")
    private com.testsigma.sdk.TestData testDataStrComma;
    @TestData(reference = "Pos")
    private com.testsigma.sdk.TestData testData3intPos;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {

        String strString = this.testDataString1.getValue().toString().trim();
        String strComma = this.testDataStrComma.getValue().toString().trim();
        String strPos = this.testData3intPos.getValue().toString().trim();
        int intPos =0;
        try {
            if (strComma.isEmpty() )
                strComma =",";

            if (strPos.equalsIgnoreCase("0") != true)
                intPos = Integer.parseInt(strPos);

            String strSplit = strString.split(strComma)[intPos];

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(strSplit);
            runTimeData.setKey("StringSplit");

            setSuccessMessage("Successfully stored String into RuntTime variable StringSplit:"+ strSplit);
            Result result = Result.SUCCESS;
            return result;
        } catch (Exception e) {
            setErrorMessage("Error for String Data" + strString +",2. " + strComma + ",3" + strPos + "-" + e.getMessage().toString());
            Result result= Result.FAILURE;
            return result;
        }

    }
} // End