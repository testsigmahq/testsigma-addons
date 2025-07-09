package com.testsigma.addons.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class CoordinateListDeserializer extends JsonDeserializer<List<Coordinate>> {
    @Override
    public List<Coordinate> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String coordinatesJson = p.getText();
        return mapper.readValue(coordinatesJson, mapper.getTypeFactory().constructCollectionType(List.class, Coordinate.class));
    }
}
