package com.testsigma.addons.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@Component
public class ObjectMapperService {

  private static final ObjectMapper failOnEmptyBeansJsonObjectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  private static final ObjectMapper parseJsonObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final ObjectMapper convertValueJsonObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final ObjectMapper simpleConvertValueJsonObjectMapper = new ObjectMapper();

  public String convertToJson(Object object) {
    try {
      return new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .writeValueAsString(object);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  public <T> T parseJson(String json, Class<T> classObject) {
    try {
      if (json != null) {
        return parseJsonObjectMapper.readValue(json, classObject);
      }
    } catch (Exception e) {
    }
    return null;
  }

  public <T> T parseJson(String json, TypeReference<T> type) {
    try {
      if (json != null) {
        return parseJsonObjectMapper
          .readValue(json, type);
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }


  public void writeValue(File file, Object classObject) {
    Writer writer = null;
    try {
      writer = new FileWriter(file);
      JsonFactory jsonFactory = new JsonFactory();
      jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
      jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
      ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      objectMapper.writeValue(writer, classObject);
      writer.flush();
      writer.close();
    } catch (IOException e) {
    }
  }

  public <T> T readValue(File file, Class<T> objectClass) {
    try {
      return parseJsonObjectMapper.readValue(file, objectClass);
    } catch (IOException e) {
    }
    return null;
  }

  public <T> T readValue(String str, TypeReference<T> typeReference) {
    try {
      if (str != null && !str.isEmpty()) {
        return null;
      }
      return parseJsonObjectMapper.readValue(str, typeReference);
    } catch (IOException e) {
    }
    return null;
  }

  public <T> T readValue(String str, Class<T> objectClass) {
    try {
      return simpleConvertValueJsonObjectMapper.readValue(str, objectClass);
    } catch (IOException e) {
    }
    return null;
  }

  public String writeValueAsString(Object object) {
    return writeValueAsString(object, false);
  }

  public String writeValueAsString(Object object, boolean failOnEmptyBeans) {
    try {
      return failOnEmptyBeans
        ? failOnEmptyBeansJsonObjectMapper.writeValueAsString(object)
        : simpleConvertValueJsonObjectMapper.writeValueAsString(object);
    } catch (IOException e) {
    }
    return null;
  }

  public <T> T convertValue(Object fromValue, Class<T> toValueType, boolean failIfUnknown) {
    try {
      if (failIfUnknown) {
        return simpleConvertValueJsonObjectMapper.convertValue(fromValue, toValueType);
      }
      return convertValueJsonObjectMapper.convertValue(fromValue, toValueType);
    } catch (Exception e) {
      return null;
    }
  }

  public <T> T convertValue(Object fromValue, Class<T> toValueType) {
    return convertValue(fromValue, toValueType, false);
  }
}
