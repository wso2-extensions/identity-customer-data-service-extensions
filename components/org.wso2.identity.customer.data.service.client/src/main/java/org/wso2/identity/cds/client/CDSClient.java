/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This class handles communication with the Customer Data Service (CDS).
 */
public class CDSClient {

    private static final Log log = LogFactory.getLog(CDSClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PROFILE_SYNC_API = "%s/o/%s/cds/api/v1/profiles/sync";
    private static final String PROFILE_SCHEMA_SYNC_API = "%s/o/%s/cds/api/v1/profile-schema/sync";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_JSON = "application/json";
    private static final String EVENT = "event";

    // Trigger identity data sync in CDS
    public static void triggerIdentityDataSync(String event, Map<String, Object> payload, String tenant) {
        payload.put(EVENT, event);
        try {
            String json = MAPPER.writeValueAsString(payload);
            String url = buildProfileSyncAPI();
            doPost(url, json, tenant, "identity-data-sync");
        } catch (IOException e) {
            log.warn("I/O error while triggering CDS identity data sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        }
    }

    // Trigger Profile data sync in CDS
    public static void triggerProfileSync(String event, Map<String, Object> payload, String tenant) {
        payload.put(EVENT, event);
        try {
            String json = MAPPER.writeValueAsString(payload);
            String url = buildProfileSyncAPI();
            doPost(url, json, tenant, "profile-sync");
        } catch (IOException e) {
            log.warn("I/O error while triggering CDS profile sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        }
    }

    public static void triggerProfileSchemasync(Map<String, Object> payload, String tenant) {
        try {
            String json = MAPPER.writeValueAsString(payload);
            String url = buildProfileSchemaSyncAPI();
            doPost(url, json, tenant, "profile-schema-sync");
        } catch (IOException e) {
            log.warn("I/O error while triggering CDS profile schema sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        }
    }

    // Execute HTTP POST request to CDS
    private static void doPost(String url, String jsonBody, String tenant, String operation) throws IOException {

        String sanitizedTenant = Utils.sanitizeForLog(tenant);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            httpPost.setHeader(AUTHORIZATION, "Basic " + Utils.getBase64EncodedCredentials());

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = "";

                if (response.getEntity() != null) {
                    String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    responseBody = body != null ? body : "";
                }

                if (statusCode == 200 || statusCode == 204) {
                    if (log.isDebugEnabled()) {
                        log.debug("CDS " + Utils.sanitizeForLog(operation) + " succeeded for tenant: " +
                                sanitizedTenant);
                    }
                    return;
                }

                CdsError cdsError = parseCdsError(responseBody);

                log.warn(String.format("CDS %s failed for tenant=%s with status=%d, code=%s, message=%s, " +
                                "description=%s",
                        Utils.sanitizeForLog(operation),
                        sanitizedTenant,
                        statusCode,
                        Utils.sanitizeForLog(cdsError.code),
                        Utils.sanitizeForLog(cdsError.message),
                        Utils.sanitizeForLog(cdsError.description)));
            }
        }
    }

    private static CdsError parseCdsError(String responseBody) {
        if (StringUtils.isBlank(responseBody)) {
            return new CdsError("n/a", "n/a", "n/a");
        }

        try {
            JsonNode node = MAPPER.readTree(responseBody);
            return new CdsError(
                    getTextOrDefault(node, "code", "n/a"),
                    getTextOrDefault(node, "message", "n/a"),
                    getTextOrDefault(node, "description", "n/a")
            );
        } catch (JsonProcessingException e) {
            return new CdsError("parse_error", "Unexpected response format", "Unable to parse error response");
        }
    }

    private static String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? defaultValue : v.asText();
    }

    private static String buildProfileSyncAPI() {
        return String.format(PROFILE_SYNC_API, Utils.getCDSServiceURL(), Utils.getCDSOrgId());
    }

    private static String buildProfileSchemaSyncAPI() {
        return String.format(PROFILE_SCHEMA_SYNC_API, Utils.getCDSServiceURL(), Utils.getCDSOrgId());
    }

    private static final class CdsError {
        private final String code;
        private final String message;
        private final String description;

        private CdsError(String code, String message, String description) {
            this.code = code;
            this.message = message;
            this.description = description;
        }
    }
}
