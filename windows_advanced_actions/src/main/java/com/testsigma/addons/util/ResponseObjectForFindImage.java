package com.testsigma.addons.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ResponseObjectForFindImage {
    @JsonProperty("isFound")
    private Boolean isFound;
    
    @JsonProperty("x1")
    private int x1;
    
    @JsonProperty("y1")
    private int y1;
    
    @JsonProperty("x2")
    private int x2;
    
    @JsonProperty("y2")
    private int y2;
    
    @JsonProperty("additionalData")
    private AdditionalData additionalData;
    
    @JsonProperty("error")
    private String error;

    @Data
    public static class AdditionalData {
        @JsonProperty("matchedPoints")
        private List<List<Integer>> matchedPoints;
    }

//    private List<Coordinate> diff_coordinates;
//    private List<Integer> image_shape;
//    private double per_similar;
//    private String scalingType;
}
