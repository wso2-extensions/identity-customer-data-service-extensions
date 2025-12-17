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

package org.wso2.identity.cds.event.handler;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.identity.cds.client.CDSClient;
import org.wso2.identity.cds.client.Utils;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.*;
import static org.wso2.identity.cds.event.handler.Constants.*;

/**
 * This class handles identity events and triggers profile syncs with the Customer Data Management Service.
 */
public class IdentityEventHandler extends AbstractEventHandler {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(IdentityEventHandler.class);

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        if (!Utils.isCDSEnabled()){
            return;
        }

        Map<String, Object> properties = event.getEventProperties();
        String eventName = event.getEventName();
        if (POST_ADD_USER.equals(eventName)) {
            try {
                Map<String, Object> userClaims = (Map<String, Object>) properties.get(USER_CLAIMS_PROPERTY);
                if (userClaims == null || userClaims.isEmpty()) {
                    LOG.error("No USER_CLAIMS found in event properties.");
                }

                // Filter userClaims to only include keys that start with "http://wso2.org/claims/"
                Map<String, Object> filteredUserClaims = userClaims.entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith(WSO2_CLAIMS_DIALECT))
                        .collect(HashMap::new, (m, e) ->
                                m.put(e.getKey(), e.getValue()), HashMap::putAll);


                String profileId = (String) filteredUserClaims.get(PROFILE_ID_CLAIM);
                String userId = (String) filteredUserClaims.get(USER_ID_CLAIM);
                Map<String, Object> profileSyncPayload = new HashMap<>();
                profileSyncPayload.put(PROFILE_ID, profileId);
                profileSyncPayload.put(USER_ID, userId);
                profileSyncPayload.put(CLAIMS, new HashMap<>(filteredUserClaims));
                profileSyncPayload.put(TENANT_ID, properties.get(TENANT_DOMAIN));
                String tenant = (String) properties.get(TENANT_DOMAIN);
                if (profileId == null || profileId.isEmpty()) {
                    CDSClient.triggerProfileSync(POST_ADD_USER, profileSyncPayload, tenant);
                    return;
                } else {
                    CDSClient.triggerIdentityDataSync(eventName, profileSyncPayload, tenant);
                }
            } catch (Exception e) {
                LOG.debug("Error handling event for CDM sync.", e);
            }
        }

        if (POST_SET_USER_CLAIM_VALUES_WITH_ID.equals(eventName) || POST_SET_USER_CLAIM_VALUE_WITH_ID.equals(eventName)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> userClaims = (Map<String, Object>) properties.get(USER_CLAIMS_PROPERTY);

                if (userClaims != null) {
                    if (userClaims.isEmpty()) {
                        throw new IdentityEventException("No USER_CLAIMS found in event properties.");
                    }

                    String userId = properties.get(USER_ID_PROPERTY).toString();
                    // Filter userClaims to only include keys that start with "http://wso2.org/claims/"
                    Map<String, Object> filteredUserClaims = userClaims.entrySet().stream()
                            .filter(entry -> entry.getKey().startsWith(WSO2_CLAIMS_DIALECT))
                            .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);

                    Map<String, Object> profileSyncPayload = new HashMap<>();
                    profileSyncPayload.put(USER_ID, userId);
                    profileSyncPayload.put(CLAIMS, new HashMap<>(filteredUserClaims));
                    profileSyncPayload.put(TENANT_ID, properties.get(TENANT_DOMAIN));
                    String tenant = (String) properties.get(TENANT_DOMAIN);
                    CDSClient.triggerIdentityDataSync(eventName, profileSyncPayload, tenant);
                }

            } catch (Exception e) {
                LOG.debug("Error handling event for CDM sync.", e);
            }
        }

        if (POST_DELETE_USER_WITH_ID.equals(eventName)) {
            try {
            String userId = properties.get(USER_ID_PROPERTY).toString();
            Map<String, Object> profileSyncPayload = new HashMap<>();
            profileSyncPayload.put(USER_ID, userId);
            profileSyncPayload.put(TENANT_ID, properties.get(TENANT_DOMAIN));
            String tenant = (String) properties.get(TENANT_DOMAIN);
            CDSClient.triggerIdentityDataSync(eventName, profileSyncPayload, tenant);
            } catch (Exception e) {
                LOG.debug("Error handling event for CDM sync.", e);
            }
        }
    }

    @Override
    public String getName() {
        return "cds.identity.event.handler";
    }
}
