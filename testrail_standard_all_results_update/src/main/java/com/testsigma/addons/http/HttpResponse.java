package com.testsigma.addons.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties
public class HttpResponse<T> {
  protected static final ObjectMapperService om = new ObjectMapperService();
  protected int statusCode;
  protected String statusMessage;
  protected String responseText;
  protected T responseEntity;
  protected Header[] responseHeaders;

  public HttpResponse(org.apache.http.HttpResponse response, TypeReference<T> typeReference) throws IOException {
    this.statusCode = response.getStatusLine().getStatusCode();
    this.statusMessage = response.getStatusLine().getReasonPhrase();
    this.responseHeaders = response.getAllHeaders();
    HttpEntity entity = response.getEntity();
    this.responseText = entity == null ? "" : EntityUtils.toString(entity);
  }

  public HttpResponse(org.apache.http.HttpResponse response) {
    this.statusCode = response.getStatusLine().getStatusCode();
    this.statusMessage = response.getStatusLine().getReasonPhrase();
    this.responseHeaders = response.getAllHeaders();
  }

  public Map<String, String> getHeadersMap() {
    Map<String, String> responseHeaders = new HashMap<>();
    for (Header header : this.responseHeaders) {
      responseHeaders.put(header.getName(), header.getValue());
    }
    return responseHeaders;
  }

}
