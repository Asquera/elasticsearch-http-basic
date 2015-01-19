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

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.Base64;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.ElasticsearchIntegrationTest.ClusterScope;
import org.elasticsearch.test.rest.client.http.HttpRequestBuilder;
import org.elasticsearch.test.rest.client.http.HttpResponse;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import static org.elasticsearch.test.ElasticsearchIntegrationTest.Scope;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test a rest action that sets special response headers
 */
@ClusterScope(transportClientRatio = 0.0, scope = Scope.SUITE, numDataNodes = 1)
public class IpAuthenticationIntegrationTest extends HttpBasicServerPluginIntegrationTest {

    protected final String whitelistedIp = "2.2.2.2";
    protected final String notWhitelistedIp = "3.3.3.3";
    protected final String trustedIp = "4.4.4.4";

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
      return builderWithPlugin()
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
    public void clientGoodCredentialsBasicAuthenticationSuceeds() throws Exception {
        HttpResponse response = requestWithCredentials("admin:admin_pw")
          .addHeader("X-Forwarded-For", "1.1.1.1" ).execute();
        assertThat(response.getStatusCode(), equalTo(RestStatus.OK.getStatus()));
    }

    @Test
    public void clientBadCredentialsBasicAuthenticationFails() throws Exception {
        HttpResponse response = requestWithCredentials("admin:wrong").execute();
        assertThat(response.getStatusCode()
            , equalTo(RestStatus.UNAUTHORIZED.getStatus()));
    }
    @Test
    public void proxyViaLocalhostIpAuthenticatesWhitelistedClients() throws Exception {
        List<String> whitelists = new ArrayList<String>();
        whitelists.add(whitelistedIp);
        whitelists.add(notWhitelistedIp + "," + whitelistedIp);
        whitelists.add(notWhitelistedIp + "," + whitelistedIp + "," + trustedIp);
        Iterator<String> iterator = whitelists.iterator();
        while (iterator.hasNext()) {
          String next = iterator.next();
          HttpResponse response = requestWithCredentials("admin:wrong")
                                  .addHeader("X-Forwarded-For", next)
                                  .execute();
          assertThat(response.getStatusCode(), equalTo(RestStatus.OK.getStatus()));
        }
    }

    @Test
    public void proxyViaLocalhostIpUnauthenticatesNonWhitelistedClients() throws Exception {
        List<String> whitelists = new ArrayList<String>();
        whitelists.add(notWhitelistedIp);
        whitelists.add(whitelistedIp + "," + notWhitelistedIp + "," + trustedIp);
        whitelists.add("");
        Iterator<String> iterator = whitelists.iterator();
        while (iterator.hasNext()) {
          String next = iterator.next();
          HttpResponse response = requestWithCredentials("admin:wrong")
                                  .addHeader("X-Forwarded-For", next)
                                  .execute();
          assertThat(response.getStatusCode(), equalTo(RestStatus.UNAUTHORIZED.getStatus()));
        }
    }

    protected HttpRequestBuilder requestWithCredentials(String credentials) throws Exception {
        return httpClient().path("/_status")
          .addHeader("Authorization", "Basic " + Base64.encodeBytes(credentials.getBytes()));
    }

}
