package com.testsigma.addons.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
public class ResponseObject {
    @JsonDeserialize(using = CoordinateListDeserializer.class)
    private List<Coordinate> diff_coordinates;
    private String error;
    private List<Integer> image_shape;
    private double per_similar;
    private String scalingType;
}
