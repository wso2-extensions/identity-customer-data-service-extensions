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
public class ClaimEventHandler extends AbstractEventHandler {

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        Map<String, Object> properties = event.getEventProperties();
        String eventName = event.getEventName();


        if ("POST_LOCAL_CLAIM_ADD".equals(event.getEventName())) {
            String localClaimUri = (String)event.getEventProperties().get("localClaimUri");
            String data = this.buildLocalClaimData(event);
        } else if ("POST_UPDATE_LOCAL_CLAIM".equals(event.getEventName())) {
            String localClaimUri = (String)event.getEventProperties().get("localClaimUri");
            String data = this.buildLocalClaimData(event);
        } else if ("POST_DELETE_LOCAL_CLAIM".equals(event.getEventName())) {
            String claimDialectUri = (String)event.getEventProperties().get("claimDialectUri");
            String claimUri = (String)event.getEventProperties().get("localClaimUri");
            String data = "Claim Dialect URI:" + claimDialectUri + ", Claim URI:" + claimUri;
        } else if ("POST_ADD_EXTERNAL_CLAIM".equals(event.getEventName())) {
            String externalClaimUri = (String)event.getEventProperties().get("externalClaimUri");
            String data = this.buildExternalClaimData(event);
        } else if ("POST_UPDATE_EXTERNAL_CLAIM".equals(event.getEventName())) {
            String externalClaimUri = (String)event.getEventProperties().get("externalClaimUri");
            String data = this.buildExternalClaimData(event);
        } else if ("POST_DELETE_EXTERNAL_CLAIM".equals(event.getEventName())) {
            String claimDialectUri = (String)event.getEventProperties().get("claimDialectUri");
            String externalClaimUri = (String)event.getEventProperties().get("externalClaimUri");
            String data = "Claim Dialect URI:" + claimDialectUri + ", Claim URI:" + externalClaimUri;
        }
    }

    @Override
    public String getName() {
        return "cds.claim.event.handler";
    }
}
