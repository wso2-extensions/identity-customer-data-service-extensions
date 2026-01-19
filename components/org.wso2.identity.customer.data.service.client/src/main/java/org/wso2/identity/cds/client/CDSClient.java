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

import com.fasterxml.jackson.databind.JsonNode;
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
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PROFILE_SYNC_API = "%s/t/%s/cds/api/v1/profiles/sync";
    private static final String PROFILE_SCHEMA_SYNC_API = "%s/t/%s/cds/api/v1/profile-schema/sync";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_JSON = "application/json";
    private static final String EVENT = "event";

    // Trigger identity data sync in CDS
    public static void triggerIdentityDataSync(String event, Map<String, Object> payload, String tenant) {

        payload.put(EVENT, event);

        try {
            String json = MAPPER.writeValueAsString(payload);
            String url = buildProfileSyncAPI(tenant);
            doPost(url, json, tenant, "identity-data-sync");
        } catch (IOException e) {
            log.warn("I/O error while triggering CDS identity data sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        } catch (RuntimeException e) {
            log.warn("Runtime error while triggering CDS identity data sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        }
    }

    // Trigger Profile data sync in CDS
    public static void triggerProfileSync(String event, Map<String, Object> payload, String tenant) {

        payload.put(EVENT, event);
        try {
            String json = MAPPER.writeValueAsString(payload);
            String url = buildProfileSyncAPI(tenant);

            doPost(url, json, tenant, "profile-sync");

        } catch (IOException e) {
            log.warn("I/O error while triggering CDS profile sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        } catch (RuntimeException e) {
            log.warn("Runtime error while triggering CDS profile sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        }
    }

    public static void triggerProfileSchemasync(Map<String, Object> payload, String tenant) {

        try {
            String json = MAPPER.writeValueAsString(payload);
            String url = buildProfileSchemaSyncAPI(tenant);

            doPost(url, json, tenant, "profile-schema-sync");

        } catch (IOException e) {
            log.warn("I/O error while triggering CDS profile schema sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        } catch (RuntimeException e) {
            log.warn("Runtime error while triggering CDS profile schema sync. tenant="
                    + Utils.sanitizeForLog(tenant), e);
        }
    }

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

                if (response.getEntity() != null && response.getEntity().getContent() != null) {
                    responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                }

                // Success: keep logs minimal. Put details in debug only.
                if (statusCode == 200 || statusCode == 204) {
                    if (log.isDebugEnabled()) {
                        log.debug("CDS " + operation + " success. status=" + statusCode + ", tenant=" +
                                sanitizedTenant);
                    }
                    return;
                }

                CdsError cdsError = parseCdsError(responseBody);

                String safeCode = Utils.sanitizeForLog(cdsError.code);
                String safeMsg = Utils.sanitizeForLog(cdsError.message);
                String safeDesc = Utils.sanitizeForLog(cdsError.description);

                log.warn("CDS " + operation + " failed. status=" + statusCode
                        + ", tenant=" + sanitizedTenant
                        + ", code=" + safeCode
                        + ", message=" + safeMsg
                        + ", description=" + safeDesc);

                if (log.isDebugEnabled()) {
                    log.debug("CDS " + operation + " raw response body (sanitized, clipped). tenant=" + sanitizedTenant
                            + ", body=" + Utils.sanitizeForLog(Utils.clip(responseBody, 2048)));
                }
            }
        }
    }

    private static CdsError parseCdsError(String responseBody) {

        if (responseBody == null || responseBody.isBlank()) {
            return new CdsError("n/a", "n/a", "n/a");
        }

        try {
            JsonNode node = MAPPER.readTree(responseBody);
            String code = getTextOrNull(node, "code");
            String message = getTextOrNull(node, "message");
            String description = getTextOrNull(node, "description");
            return new CdsError(
                    code != null ? code : "n/a",
                    message != null ? message : "n/a",
                    description != null ? description : "n/a"
            );

        } catch (Exception ignore) {
            return new CdsError("n/a", "Unexpected response", "Unable to parse error response");
        }
    }

    private static String getTextOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        return v.asText();
    }

    // Build the Profile Sync API URL
    private static String buildProfileSyncAPI(String tenant) {
        return String.format(PROFILE_SYNC_API, Utils.getCDSServiceURL(), tenant);
    }

    private static String buildProfileSchemaSyncAPI(String tenant) {
        return String.format(PROFILE_SCHEMA_SYNC_API, Utils.getCDSServiceURL(), tenant);
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
