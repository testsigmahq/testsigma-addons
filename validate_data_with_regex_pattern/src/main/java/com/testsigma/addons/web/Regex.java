package com.testsigma.addons.web;

import com.testsigma.sdk.Result;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex
{
    public static void main(String[] args) {
        try {
            Pattern pattern = Pattern.compile("^[A-Za-z]+$");
            Matcher matcher = pattern.matcher("HelloWorld");
            if (matcher.matches()) {
                System.out.println("The input string matches the pattern.");
            } else {
                System.out.println("The input string does not match the pattern.");
            }
        } catch (Exception error) {
            System.out.println("Exception occurred while executing the action::" + error.getMessage());
        }
    }
}
