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

import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.identity.cds.client.CDSClient;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles identity events and triggers profile syncs with the Customer Data Management Service.
 */
public class IdentityEventHandler extends AbstractEventHandler {

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        Map<String, Object> properties = event.getEventProperties();
        String eventName = event.getEventName();


        if ("POST_ADD_USER".equals(eventName)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> userClaims = (Map<String, Object>) properties.get("USER_CLAIMS");

                System.out.println("==== Event: " + eventName + " ====");

                if (userClaims == null || userClaims.isEmpty()) {
                    throw new IdentityEventException("No USER_CLAIMS found in event properties.");
                }

                // Filter userClaims to only include keys that start with "http://wso2.org/claims/"
                Map<String, Object> filteredUserClaims = userClaims.entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith("http://wso2.org/claims/"))
                        .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);


                String profileId = (String) filteredUserClaims.get("http://wso2.org/claims/profileId");


                String userId = (String) filteredUserClaims.get("http://wso2.org/claims/userid");

                Map<String, Object> profileSyncPayload = new HashMap<>();
                profileSyncPayload.put("profileId", profileId);
                profileSyncPayload.put("userId", userId);
                profileSyncPayload.put("claims", new HashMap<>(filteredUserClaims));
                profileSyncPayload.put("tenantId", properties.get("tenant-domain"));
                String tenant = (String) properties.get("tenant-domain");

                if (profileId == null || profileId.isEmpty()) {
//                    System.out.println("No profileId found. Skipping CDM sync.");
                    CDSClient.triggerProfileSync("POST_ADD_USER", profileSyncPayload, tenant);
                    return;
                } else {
                    CDSClient.triggerIdentityDataSync(eventName, profileSyncPayload, tenant);
                }

                System.out.println("Profile sync pushed successfully for profileId: " + profileId);

            } catch (Exception e) {
                throw new IdentityEventException("Error handling event for CDM sync.", e);
            }
        }

        //todo: for existing this will only does the update. So there is a chance that the existing claims are not synced. We need to handle that scenario as well.
        if ("POST_SET_USER_CLAIM_VALUES_WITH_ID".equals(eventName) || "POST_SET_USER_CLAIM_VALUE_WITH_ID".equals(eventName)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> userClaims = (Map<String, Object>) properties.get("USER_CLAIMS");

                if (userClaims != null) {


                    for (Map.Entry<String, Object> entry : userClaims.entrySet()) {
                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue() + ", Type: " + entry.getValue().getClass());
                    }

                    if (userClaims.isEmpty()) {
                        throw new IdentityEventException("No USER_CLAIMS found in event properties.");
                    }

                    String userId = properties.get("USER_ID").toString();

                    // Filter userClaims to only include keys that start with "http://wso2.org/claims/"
                    Map<String, Object> filteredUserClaims = userClaims.entrySet().stream()
                            .filter(entry -> entry.getKey().startsWith("http://wso2.org/claims/"))
                            .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);


                    Map<String, Object> profileSyncPayload = new HashMap<>();
                    profileSyncPayload.put("userId", userId);
                    profileSyncPayload.put("claims", new HashMap<>(filteredUserClaims));
                    profileSyncPayload.put("tenantId", properties.get("tenant-domain"));
                    String tenant = (String) properties.get("tenant-domain");


                    CDSClient.triggerIdentityDataSync(eventName, profileSyncPayload, tenant);

                    System.out.println("Profile sync pushed successfully for userId: " + userId);
                }

            } catch (Exception e) {
                throw new IdentityEventException("Error handling event for CDM sync.", e);
            }
        }

        if ("POST_DELETE_USER_WITH_ID".equals(eventName)) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }
            try {
            String userId = properties.get("USER_ID").toString();
            Map<String, Object> profileSyncPayload = new HashMap<>();
            profileSyncPayload.put("userId", userId);
            profileSyncPayload.put("tenantId", properties.get("tenant-domain"));
            String tenant = (String) properties.get("tenant-domain");
            CDSClient.triggerIdentityDataSync(eventName, profileSyncPayload, tenant);
            return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public String getName() {
        return "cds.identity.event.handler";
    }
}
