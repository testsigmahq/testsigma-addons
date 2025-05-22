/*
 *
 * ****************************************************************************
 *  * Copyright (C) 2019 Testsigma Inc.
 *  * All rights reserved.
 *  ****************************************************************************
 *
 */

package com.testsigma.addons.http;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.Header;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;


@Component
public class HttpClient {

  public final static int MAX_RETRIES = 3;
  public final static int WAITING_PERIOD_BETWEEN_RETRIES = 45000;

  private static final RequestConfig config = RequestConfig.custom()
    .setSocketTimeout(10 * 60 * 1000)
    .setConnectionRequestTimeout(60 * 1000)
    .setConnectTimeout(60 * 1000)
    .build();


  private CloseableHttpClient getClient(final int retryCount, final int waitTime) {
    return HttpClientBuilder.create()
      .setDefaultRequestConfig(config)
      .setRetryHandler((exception, executionCount, context) -> {
        if (executionCount<retryCount) {
          return true;
        }
        return false;
      })
      .setServiceUnavailableRetryStrategy(new ServiceUnavailableRetryStrategy() {
        @Override
        public boolean retryRequest(org.apache.http.HttpResponse response, int executionCount, HttpContext context) {
          if (executionCount < retryCount && response.getStatusLine().getStatusCode() >= 502) {
            return true;
          }
          return false;
        }
        @Override
        public long getRetryInterval() {
          return waitTime;
        }
      }).build();
  }


  /* ----------------------------------- */
  /* -------------- GET ---------------- */
  /* ----------------------------------- */

  public <T> HttpResponse<T> get(String url, List<Header> authHeader, TypeReference<T> typeReference) throws IOException {
    return get(url, typeReference, authHeader, MAX_RETRIES, WAITING_PERIOD_BETWEEN_RETRIES);
  }

  public <T> HttpResponse<T> get(String url, TypeReference<T> ref, List<Header> authHeader, int retryCount, int waitTime)
    throws IOException {

    try (CloseableHttpClient client = getClient(retryCount, waitTime)) {
      HttpGet getRequest = new HttpGet(url);
      addHeaders(getRequest, authHeader);
      try (CloseableHttpResponse res = client.execute(getRequest)) {
        return new HttpResponse<>(res, ref);
      }
    }
  }

  private void addHeaders(HttpRequestBase request, List<Header> authHeader) {
    request.setHeaders(authHeader.toArray(new Header[0]));
  }

}
