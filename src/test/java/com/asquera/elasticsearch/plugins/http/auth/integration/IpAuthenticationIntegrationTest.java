
/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.asquera.elasticsearch.plugins.http.auth.integration;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.Base64;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.ElasticsearchIntegrationTest.ClusterScope;
import org.elasticsearch.test.rest.client.http.HttpGetWithEntity;
import org.elasticsearch.test.rest.client.http.HttpRequestBuilder;
import org.elasticsearch.test.rest.client.http.HttpResponse;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.elasticsearch.test.ElasticsearchIntegrationTest.Scope;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test a rest action that sets special response headers
 */
@ClusterScope(transportClientRatio = 0.0, scope = Scope.SUITE, numDataNodes = 1)
public class IpAuthenticationIntegrationTest extends ElasticsearchIntegrationTest {

    protected final String localhost = "127.0.0.1";
    protected final String whitelistedIp = "2.2.2.2";
    protected final String notWhitelistedIp = "3.3.3.3";
    protected final String trustedIp = "4.4.4.4";

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return ImmutableSettings.settingsBuilder()
                .putArray("http.basic.ipwhitelist", whitelistedIp)
                .putArray("http.basic.trusted_proxy_chains", trustedIp + "," + localhost)
                .put("http.basic.xforward", "X-Forwarded-For")
                .build();
    }

    @Test
    public void testHealthCheck() throws Exception {
        HttpResponse response = httpClient().path("/").execute();
        assertThat(response.getStatusCode(), equalTo(RestStatus.OK.getStatus()));
    }

    @Test
    public void localhostClientIsBasicAuthenticated() throws Exception {
        HttpUriRequest request = httpRequest();
        String credentials = "admin:admin_pw";
        request.setHeader("Authorization", "Basic " + Base64.encodeBytes(credentials.getBytes()));
        CloseableHttpResponse response = closeableHttpClient().execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(RestStatus.OK.getStatus()));
    }

    @Test
    public void proxyViaLocalhostIpAuthenticatesWhitelistedClients() throws Exception {
        HttpUriRequest request = httpRequest();
        request.setHeader("X-Forwarded-For", whitelistedIp );
        CloseableHttpResponse response = closeableHttpClient().execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(RestStatus.OK.getStatus()));
        request = httpRequest();
        request.setHeader("X-Forwarded-For", notWhitelistedIp + "," + whitelistedIp);
        response = closeableHttpClient().execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(RestStatus.OK.getStatus()));
        request = httpRequest();
        request.setHeader("X-Forwarded-For", notWhitelistedIp + "," + whitelistedIp + "," + trustedIp);
        response = closeableHttpClient().execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(RestStatus.OK.getStatus()));
    }

    @Test
    public void proxyViaLocalhostIpUnauthenticatesNonWhitelistedClients() throws Exception {
        HttpUriRequest request = httpRequest();
        request.setHeader("X-Forwarded-For", notWhitelistedIp);
        CloseableHttpResponse response = closeableHttpClient().execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(RestStatus.UNAUTHORIZED.getStatus()));
        request = httpRequest();
        request.setHeader("X-Forwarded-For", whitelistedIp + "," + notWhitelistedIp + "," + trustedIp);
        response = closeableHttpClient().execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(RestStatus.UNAUTHORIZED.getStatus()));
        request = httpRequest();
        request.setHeader("X-Forwarded-For", "");
        response = closeableHttpClient().execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(RestStatus.UNAUTHORIZED.getStatus()));
    }
    
    public static HttpRequestBuilder httpClient() {
        return new HttpRequestBuilder(HttpClients.createDefault())
          .host("localhost").port(9200);
    }
   
    public static HttpUriRequest httpRequest() {
      HttpUriRequest httpUriRequest = null;
          try {
            httpUriRequest = new HttpGetWithEntity(new URI("http",
                  null, "localhost", 9200, "/_status", null, null));
                } catch (URISyntaxException e) {
                  throw new IllegalArgumentException(e);
                }
      return httpUriRequest;
    }

    public static CloseableHttpClient closeableHttpClient() {
      return HttpClients.createDefault();
    }
    
}
