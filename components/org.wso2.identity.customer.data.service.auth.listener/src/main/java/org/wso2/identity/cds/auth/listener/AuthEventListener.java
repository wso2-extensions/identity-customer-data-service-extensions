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
import org.wso2.identity.cds.client.Utils;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.POST_AUTHENTICATION;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.AUTHENTICATION_SUCCESS;

/**
 * This class contains the implementation of the Authentication event listener.
 * This handles authentication success events and communicates to CDS.
 */
public class AuthEventListener extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(AuthEventListener.class);
    public static final String PROFILE_ID = "profileId";
    public static final String USER_ID = "userId";
    public static final String TENANT_ID = "tenantId";
    public static final String TENANT_DOMAIN = "tenant-domain";
    public static final String USER_TENANT_DOMAIN = "user-tenant-domain";

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        if (!Utils.isCDSEnabled()){
            return;
        }

        if (AUTHENTICATION_SUCCESS.equals(event.getEventName())) {
            AuthenticationContext context = (AuthenticationContext) event.getEventProperties().get("context");
            if (context != null) {
                String cookieValue = (String) context.getProperty(PROFILE_ID);
                LOG.debug("Cookie captured during login: " + cookieValue);
                if (cookieValue == null || cookieValue.isEmpty()) {
                    LOG.debug("No profileId cookie found in the authentication context.");
                    return;
                }
                try {
                    String userId;
                    userId = context.getSequenceConfig().getAuthenticatedUser().getUserId();
                    Map<String, Object> profileSyncPayload = new HashMap<>();
                    profileSyncPayload.put(PROFILE_ID, cookieValue);
                    profileSyncPayload.put(USER_ID, userId);
                    profileSyncPayload.put(TENANT_ID, context.getProperty(USER_TENANT_DOMAIN));
                    String tenant = context.getTenantDomain();
                    CDSClient.triggerIdentityDataSync(event.getEventName(), profileSyncPayload, tenant);
                }
                catch (UserIdNotFoundException e) {
                    LOG.warn("User ID not found in authentication context.", e);
                }
            }
        }


        if (POST_AUTHENTICATION.equals(event.getEventName())) {
            AuthenticationContext context = (AuthenticationContext) event.getEventProperties().get("context");
            if (context != null) {
                String tenant = context.getTenantDomain();
                String cookieValue = (String) context.getProperty(PROFILE_ID);
                if (cookieValue == null || cookieValue.isEmpty()) {
                    LOG.warn("No profileId cookie found in the authentication context.");
                    return;
                }
                try {
                    String userId;
                    userId = context.getSequenceConfig().getAuthenticatedUser().getUserId();
                    Map<String, Object> profileSyncPayload = new HashMap<>();
                    profileSyncPayload.put(PROFILE_ID, cookieValue);
                    profileSyncPayload.put(USER_ID, userId);
                    profileSyncPayload.put(TENANT_ID, properties.get(TENANT_DOMAIN));
                    CDSClient.triggerIdentityDataSync(event.getEventName(), profileSyncPayload, tenant);
                }
                catch (UserIdNotFoundException e) {
                    LOG.warn("User ID not found in authentication context.", e);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "cds.authentication.listener";
    }

}