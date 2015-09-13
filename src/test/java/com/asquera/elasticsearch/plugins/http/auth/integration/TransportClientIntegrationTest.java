
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

import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Test;

import org.elasticsearch.test.ElasticsearchIntegrationTest.ClusterScope;
import static org.elasticsearch.test.ElasticsearchIntegrationTest.Scope;
import static org.hamcrest.Matchers.equalTo;

import org.elasticsearch.client.Client;
import static org.elasticsearch.client.Requests.createIndexRequest;
import static org.elasticsearch.client.Requests.refreshRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse.*;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;

/**
 * Test the transport client is compatible with the plugin
 */
@ClusterScope(transportClientRatio = 1.0, scope = Scope.SUITE, numDataNodes = 1)
public class TransportClientIntegrationTest extends HttpBasicServerPluginIntegrationTest {

    @Test
    public void testHealthCheck() throws Exception {
        Client client = ElasticsearchIntegrationTest.client();
        logger.info("-->creating index [testto]");
        client.admin().indices()
            .create(createIndexRequest("testto")).actionGet();
        client.admin().indices()
            .refresh(refreshRequest()).actionGet();
        logger.info("-->cluster_health");
        ClusterHealthResponse clusterHealth = client.admin().cluster().
            health((new ClusterHealthRequest("testto"))
                    .waitForGreenStatus()).actionGet();
        logger.info("-->cluster_health, status " + clusterHealth.getStatus());
        assertThat(clusterHealth.isTimedOut(),equalTo(false));
        assertThat(clusterHealth.getStatus(),equalTo(ClusterHealthStatus.GREEN));
    }

}
