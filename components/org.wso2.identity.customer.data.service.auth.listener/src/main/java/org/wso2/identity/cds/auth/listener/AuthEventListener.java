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

package org.wso2.identity.cds.auth.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.identity.cds.client.CDSClient;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the implementation of the Authentication event listener.
 * This handles authentication success events.
 */
public class AuthEventListener extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(AuthEventListener.class);


    @Override
    public void handleEvent(Event event) throws IdentityEventException {


        if ("AUTHENTICATION_SUCCESS".equals(event.getEventName())) {
            LOG.info("Handling AUTHENTICATION_SUCCESS event");

            AuthenticationContext context = (AuthenticationContext) event.getEventProperties().get("context");


            if (context != null) {
                String cookieValue = (String) context.getProperty("profileId");
                LOG.info("Cookie captured during login: " + cookieValue);
                if (cookieValue == null || cookieValue.isEmpty()) {
                    LOG.warn("No profileId cookie found in the authentication context.");
                    return;
                }
                try {
                    String userId;

                    userId = context.getSequenceConfig().getAuthenticatedUser().getUserId();

                    Map<String, Object> profileSyncPayload = new HashMap<>();

                    profileSyncPayload.put("profileId", cookieValue);
                    profileSyncPayload.put("userId", userId);
                    profileSyncPayload.put("tenantId", context.getProperty("user-tenant-domain"));
                    String tenant = context.getTenantDomain();
                    CDSClient.triggerIdentityDataSync(event.getEventName(), profileSyncPayload, tenant);
                }
                catch (UserIdNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }
        }

//
//        if ("POST_AUTHENTICATION".equals(event.getEventName())) {
//            LOG.info("Handling POST_AUTHENTICATION event");
//            AuthenticationContext context = (AuthenticationContext) event.getEventProperties().get("context");
//
//            for (Map.Entry<String, Object> entry : event.getEventProperties().entrySet()) {
//                LOG.info("Event Property - Key: " + entry.getKey() + ", Value: " + entry.getValue());
//            }
//
//            if (context != null) {
//                String cookieValue = (String) context.getProperty("profileId");
//                LOG.info("Cookie captured during login: " + cookieValue);
//
//                if (cookieValue == null || cookieValue.isEmpty()) {
//                    LOG.warn("No profileId cookie found in the authentication context.");
//                    return;
//                }
//                try {
//
//                    String userId;
//
//                    userId = context.getSequenceConfig().getAuthenticatedUser().getUserId();
//
//                    Map<String, Object> profileSyncPayload = new HashMap<>();
//
//                    profileSyncPayload.put("profileId", cookieValue);
//                    profileSyncPayload.put("userId", userId);
//                    profileSyncPayload.put("tenantId", properties.get("tenant-domain"));
//
//                    CDMClient.triggerIdentityDataSync(event.getEventName(), profileSyncPayload);
//                }
//                catch (UserIdNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//
//            }
//        }
    }

    @Override
    public String getName() {
        return "cds.auth.listener";
    }

}