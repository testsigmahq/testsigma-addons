package com.testsigma.addons.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OCRResponse {
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("text")
    private List<OCRTextPoint> text;
    
    // Manual getter methods since Lombok annotation processor is not working
    public String getError() { return error; }
    public List<OCRTextPoint> getText() { return text; }
    
    // Helper method to check if there are any errors
    public boolean hasError() {
        return error != null && !error.trim().isEmpty();
    }
    
    // Helper method to check if text was found
    public boolean hasText() {
        return text != null && !text.isEmpty();
    }
}
