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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.IdentityEventClientException;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.IdentityEventServerException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.identity.cds.client.CDSClient;
import org.wso2.identity.cds.client.CDSClientException;
import org.wso2.identity.cds.client.Utils;

import java.util.Map;

import static org.wso2.identity.cds.event.handler.Constants.CDS_ENABLED_PROPERTY;
import static org.wso2.identity.cds.event.handler.Constants.PRE_UPDATE_CDS_CONFIG;
import static org.wso2.identity.cds.event.handler.Constants.TENANT_DOMAIN;

/**
 * Sends the tenant CDS enable config to the Customer Data Service (CDS) before Identity Server stores it.
 * If CDS does not accept the value, the handler throws an exception, and Identity Server does not store the value.
 * This handler does not check whether CDS is enabled, because it must also send the value that enables CDS.
 */
public class CDSConfigEventHandler extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(CDSConfigEventHandler.class);

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        if (!PRE_UPDATE_CDS_CONFIG.equals(event.getEventName())) {
            return;
        }

        Map<String, Object> properties = event.getEventProperties();
        Object tenantDomain = properties == null ? null : properties.get(TENANT_DOMAIN);
        Object cdsEnabled = properties == null ? null : properties.get(CDS_ENABLED_PROPERTY);
        if (!(tenantDomain instanceof String) || StringUtils.isBlank((String) tenantDomain) ||
                !(cdsEnabled instanceof Boolean)) {
            throw new IdentityEventServerException("The " + PRE_UPDATE_CDS_CONFIG + " event must have the " +
                    TENANT_DOMAIN + " and " + CDS_ENABLED_PROPERTY + " properties.");
        }

        try {
            CDSClient.updateCDSConfig((String) tenantDomain, (Boolean) cdsEnabled);
        } catch (CDSClientException e) {
            if (e.isClientError()) {
                throw new IdentityEventClientException(e.getMessage(), e);
            }
            throw new IdentityEventServerException(e.getMessage(), e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sent the CDS config to CDS for tenant: " + Utils.sanitizeForLog(tenantDomain));
        }
    }

    @Override
    public String getName() {
        return "cds.config.event.handler";
    }
}
