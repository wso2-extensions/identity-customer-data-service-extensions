/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.cds.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This class handles communication with the Customer Data Service (CDS).
 */
public class CDSClient {

    private static final Log log = LogFactory.getLog(CDSClient.class);
    private static final String PROFILE_SYNC_API = "%s/t/%s/cds/api/v1/profiles/sync";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_JSON = "application/json";
    private static final String EVENT = "event";

    public static void triggerIdentityDataSync(String event, Map<String, Object> payload, String tenant) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            payload.put(EVENT, event);
            String json = mapper.writeValueAsString(payload);
            String profileSyncUrl = buildProfileSyncAPI(tenant);

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(profileSyncUrl);
                httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
                httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
                httpPost.setHeader(AUTHORIZATION, "Basic " + Utils.getBase64EncodedCredentials());

                try (CloseableHttpResponse response = client.execute(httpPost)) {
                    int statusCode = response.getStatusLine().getStatusCode();

                    String responseBody = "";
                    if (response.getEntity() != null) {
                        responseBody = new String(response.getEntity().getContent().readAllBytes(),
                                StandardCharsets.UTF_8);
                    }

                    log.info("CDM sync response status: " + statusCode + ", body: " + responseBody);
                    if (statusCode != 200 && statusCode != 204) {
                        log.warn("CDM sync failed for identity data. Status: " + statusCode);
                    }
                }
            }
        } catch (IOException e) {
            log.error("I/O error occurred while triggering identity data sync for tenant: " + tenant, e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while triggering identity data sync for tenant: " + tenant, e);
        }
    }

    public static void triggerProfileSync(String event, Map<String, Object> payload, String tenant) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            payload.put(EVENT, event);
            String json = mapper.writeValueAsString(payload);
            String profileSyncUrl = buildProfileSyncAPI(tenant);
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(profileSyncUrl);
                httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
                httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
                httpPost.setHeader(AUTHORIZATION, "Basic " + Utils.getBase64EncodedCredentials());

                try (CloseableHttpResponse response = client.execute(httpPost)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = "";
                    if (response.getEntity() != null) {
                        responseBody = new String(response.getEntity().getContent().readAllBytes(),
                                StandardCharsets.UTF_8);
                    }

                    log.info("CDM sync response status: " + statusCode + ", body: " + responseBody);
                    if (statusCode != 200 && statusCode != 204) {
                        log.error("Failed to sync profile data to CDS. Status: " +
                                statusCode + ", Response: " + responseBody);
                    }
                }
            }
        } catch (IOException e) {
            log.error("I/O error occurred while triggering profile sync for tenant: " + tenant, e);
        } catch (RuntimeException e) {
            log.error("Profile sync failed for tenant: " + tenant + " due to business logic error.", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while triggering profile sync for tenant: " + tenant, e);
        }
    }

    // Build the Profile Sync API URL
    private static String buildProfileSyncAPI(String tenant) {
        return String.format(
                PROFILE_SYNC_API,
                Utils.getCDMServiceURL(),
                tenant
        );
    }
}
