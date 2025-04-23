package com.testsigma.addons.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Collections;

public class GoogleSheetsAuthenticationUtil {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static Sheets getSheetsService(String credentialsPath,String applicationName) throws Exception {
        File tempFile = urlToFileConverter(credentialsPath);
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(tempFile.getAbsolutePath()))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(applicationName).build();
    }
    private static File  urlToFileConverter(String url) throws  Exception {
        if (url.startsWith("https://")) {
            URL urlObject = new URL(url);
            File tempFile = File.createTempFile("tempExcelFile_", "." + "json");
            FileUtils.copyURLToFile(urlObject, tempFile);
            return tempFile;
        } else {
            // ignoring " and ' from file path...
            url = url.replaceAll("[\"']","");
            return new File(url);
        }
    }
}
