package com.testsigma.addons.utils;

import com.jayway.jsonpath.JsonPath;

public class JSONUtilities {

    public static String readJsonData(String jsonString, String jsonPath){
        return JsonPath.read(jsonString, jsonPath).toString();
    }

}
