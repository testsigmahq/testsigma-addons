package com.testsigma.addons.web;

import lombok.Data;

@Data
public class StringCompareUtil {
    private String errorMessage;
    private String successMessage;

    public boolean performOperation(String string1, String string2, String operation){
        boolean equalsCheck = false;
        boolean containsCheck = false;
        this.errorMessage = "Not a valid operator("+operation+")";

        switch (operation) {
            case "equals":
                if (string1.equals(string2)) {
                    equalsCheck = true;
                    this.successMessage = "Both the strings match: " + string1 + " == " + string2;
                } else {
                    this.errorMessage = "Strings do not match. Value1: " + string2 + ", Value2: " + string1;
                }
                break;
            case "equals ignore-case":
                if (string1.equalsIgnoreCase(string2)) {
                    equalsCheck = true;
                    this.successMessage = "Both the strings match (ignore case): " + string1 + " == " + string2;
                } else {
                    this.errorMessage = "Strings do not match (ignore case). Value1: " + string2 + ", Value2: " + string1;
                }
                break;
            case "contains":
                if (string1.contains(string2)) {
                    containsCheck = true;
                    this.successMessage =  string1 + " contains " + string2;
                } else {
                    this.errorMessage = "Value1 does not contain Value2. Value1: " + string1 + ", Value2: " + string2;
                }
                break;
            case "contains ignore-case":
                if (string1.toLowerCase().contains(string2.toLowerCase())) {
                    containsCheck = true;
                    this.successMessage =  string1 + " contains (ignore case) " + string2;
                } else {
                    this.errorMessage = "Value1 does not contain Value2 (ignore case). Value1: " + string1 + ", Value2: " + string2;
                }
                break;
        }
        if(equalsCheck || containsCheck){
            return true;
        } else {
            return false;
        }
    }
}
