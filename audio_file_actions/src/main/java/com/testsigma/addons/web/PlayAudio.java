package com.testsigma.addons.web;

import com.testsigma.addons.util.AudioPlaybackUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;


@Data
@Action(actionText = "play audio from file file-path-or-url (only .mp3 format is supported)",
        description = "Plays audio from a specified file path or URL. Only .mp3 format is supported.",
        applicationType = ApplicationType.WEB)
public class PlayAudio extends WebAction {

    @TestData(reference = "file-path-or-url")
    private com.testsigma.sdk.TestData filePath;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            String inputValue = filePath.getValue().toString();
            logger.info("Playing audio from: " + inputValue);

            AudioPlaybackUtil audioPlaybackUtil = new AudioPlaybackUtil(logger);
            String fileName = audioPlaybackUtil.extractFileName(inputValue);
            audioPlaybackUtil.validateFormat(fileName);

            File audioFile = audioPlaybackUtil.urlToFileConverter(fileName, inputValue);
            audioPlaybackUtil.playMp3(audioFile);

            setSuccessMessage("Successfully played audio from file: " + fileName);
        } catch (IllegalArgumentException e) {
            logger.warn("Unsupported audio format: " + e.getMessage());
            setErrorMessage(e.getMessage());
            result = Result.FAILED;
        } catch (Exception e) {
            logger.warn("Failed to play audio: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to play audio: " + e.getMessage());
            result = Result.FAILED;
        }

        return result;
    }
}
