package com.testsigma.addons.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class TableActionUtils {

    public static List<WebElement> getRows(WebElement table) {
        return table.findElements(By.tagName("tr"));
    }

    public static List<WebElement> getHeaders(WebElement table) {
        return table.findElements(By.tagName("th"));
    }

    public static List<WebElement> getCellsOfRow(WebElement row) {
        return row.findElements(By.cssSelector("td, th"));
    }

    public static int getColumnIndex(String columnName, WebElement table) throws Exception {
        List<WebElement> headers = getHeaders(table);
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().equals(columnName)) {
                return i + 1;
            }
        }
        throw new Exception("Specified column is not found in given table element");
    }
}
