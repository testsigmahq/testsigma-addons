package com.testsigma.addons.windowsLite;



import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;


@Action(actionText = "lite: Right Click on coordinates x: \"x-coordinate\" y: \"y-coordinate\"",
        description = "Right Click on coordinates",
        applicationType = ApplicationType.WINDOWS_UFT)
public class RightClick extends WindowsAction {

    @TestData(reference = "x-coordinate")
    private com.testsigma.sdk.TestData xCoordinate;

    @TestData(reference = "y-coordinate")
    private com.testsigma.sdk.TestData yCoordinate;

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            int x = Integer.parseInt(xCoordinate.getValue().toString());
            int y = Integer.parseInt(yCoordinate.getValue().toString());

            java.awt.Robot robot = new java.awt.Robot();
            robot.mouseMove(x, y);
            robot.mousePress(java.awt.event.InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(java.awt.event.InputEvent.BUTTON3_DOWN_MASK);

            setSuccessMessage(String.format("Successfully performed right click at coordinates (%d, %d)", x, y));
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Failed to perform right click at the specified coordinates: " + e.getMessage());
            logger.debug("Error performing right click at coordinates" + e);
        }
        return result;
    }
}
