package RemoveSPLCTR;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
@Action(actionText = "Remove Special char testdata1 from testdata2 and store it in runtime Variable",
        description = "Remove special character and store in a runtime variable",
        applicationType = ApplicationType.WEB)
public class RemoveSpecialcharacter extends WebAction 
{

    @TestData(reference = "testdata1")
    private com.testsigma.sdk.TestData testData1;
 
    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "Variable")
    private com.testsigma.sdk.TestData runtimeVar; 

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException 
    {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;

        try
        {
            logger.debug("testdata1 => " + testData1.getValue().toString());
            logger.debug("testdata2 => " + testData2.getValue().toString());
            System.out.println("testdata1 => " + testData1.getValue().toString());
            System.out.println("testdata2 => " + testData2.getValue().toString());
            
    		String Regex = testData1.getValue().toString();
    		String data = testData2.getValue().toString();
            if(Regex.contains("\\\\")){
                Regex = Regex.replaceAll("\\\\\\\\", "\\\\");
            }
            String RES = data.replaceAll(Regex,"");

            runTimeData.setKey(runtimeVar.getValue().toString());
            runTimeData.setValue(RES);

            System.out.println("Successfully removed special characters from " + data + " and stored the Data " + RES + " in runtime variable => " + runtimeVar.getValue().toString());
            setSuccessMessage("Successfully removed special characters from " + data + " and stored the Data " + RES + " in runtime variable => " + runtimeVar.getValue().toString());

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage().toString());
            setErrorMessage("Operation failed , the error message is ::::"+e.getMessage());
            result = Result.FAILED;
        }
        return result;
    }}

