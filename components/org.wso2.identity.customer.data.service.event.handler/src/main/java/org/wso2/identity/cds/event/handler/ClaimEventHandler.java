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

package org.wso2.identity.cds.event.handler;

import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Reference;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.identity.cds.client.CDSClient;
import org.wso2.identity.cds.client.Utils;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.identity.cds.event.handler.Constants.EVENT;
import static org.wso2.identity.cds.event.handler.Constants.TENANT_ID;

/**
 * This class handles identity events and triggers profile syncs with the Customer Data Management Service.
 */
public class ClaimEventHandler extends AbstractEventHandler {
    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(ClaimEventHandler.class);

    @Reference
    private RealmService realmService;

    @Override
    public void handleEvent(Event event) throws IdentityEventException {


        if (!Utils.isCDSEnabled()) {
            return;
        }
        String eventName = event.getEventName();
         if ("POST_ADD_EXTERNAL_CLAIM".equals(eventName) || "POST_UPDATE_EXTERNAL_CLAIM".equals(eventName) ||
                "POST_DELETE_EXTERNAL_CLAIM".equals(eventName) || "POST_UPDATE_LOCAL_CLAIM".equals(eventName)) {
            int tenantId = (int) event.getEventProperties().get("tenantId");
            String tenantDomain;
            tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            Map<String, Object> profileSchemaSyncPayload = new HashMap<>();
            profileSchemaSyncPayload.put(EVENT, eventName);
            profileSchemaSyncPayload.put(TENANT_ID, tenantDomain);
            CDSClient.triggerProfileSchemasync(profileSchemaSyncPayload, tenantDomain);
        }
    }

    @Override
    public String getName() {
        return "cds.claim.event.handler";
    }
}
