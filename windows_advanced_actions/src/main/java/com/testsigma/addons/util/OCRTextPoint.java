package com.testsigma.addons.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OCRTextPoint {
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("x1")
    private double x1;
    
    @JsonProperty("x2")
    private double x2;
    
    @JsonProperty("y1")
    private double y1;
    
    @JsonProperty("y2")
    private double y2;
    
    // Manual getter methods since Lombok annotation processor is not working
    public String getText() { return text; }
    public double getX1() { return x1; }
    public double getX2() { return x2; }
    public double getY1() { return y1; }
    public double getY2() { return y2; }
    
    // Manual setter methods
    public void setText(String text) { this.text = text; }
    public void setX1(double x1) { this.x1 = x1; }
    public void setX2(double x2) { this.x2 = x2; }
    public void setY1(double y1) { this.y1 = y1; }
    public void setY2(double y2) { this.y2 = y2; }
    
    // Helper method to get center coordinates for clicking
    public double getCenterX() {
        return (x1 + x2) / 2.0;
    }
    
    public double getCenterY() {
        return (y1 + y2) / 2.0;
    }
}
