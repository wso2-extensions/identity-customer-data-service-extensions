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

package org.wso2.identity.cds.client.internal;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.identity.cds.client.CDSClient;
import org.wso2.identity.cds.client.CDSClientException;
import org.wso2.identity.cds.client.Utils;
import org.wso2.identity.cds.client.config.CDSConfigService;

import java.util.Collections;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS;

/**
 * Implementation of {@link CDSConfigService}. The value is in the config store: resource type CDS_CONFIGURATION,
 * resource cds-config, attribute enabled.
 */
public class CDSConfigServiceImpl implements CDSConfigService {

    @Override
    public boolean isCDSEnabled(String tenantDomain) throws CDSClientException {

        if (!Utils.isCDSEnabled()) {
            return false;
        }
        try {
            return Utils.getTenantCDSEnabled(Utils.getRootTenantDomain(tenantDomain)).orElse(false);
        } catch (OrganizationManagementException | ConfigurationManagementException e) {
            throw new CDSClientException("Error while reading the CDS config of the tenant: " +
                    Utils.sanitizeForLog(tenantDomain), e);
        }
    }

    @Override
    public void updateCDSEnabled(String tenantDomain, boolean cdsEnabled) throws CDSClientException {

        if (!Utils.isCDSEnabled()) {
            throw new CDSClientException("CDS is not enabled for the server.", true);
        }
        // CDS gets the value first. If CDS does not accept it, the value is not stored.
        CDSClient.updateCDSConfig(tenantDomain, cdsEnabled);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain);
            carbonContext.setTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
            storeCDSEnabled(cdsEnabled);
        } catch (ConfigurationManagementException e) {
            throw new CDSClientException("Error while storing the CDS config of the tenant: " +
                    Utils.sanitizeForLog(tenantDomain), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void storeCDSEnabled(boolean cdsEnabled) throws ConfigurationManagementException, CDSClientException {

        ConfigurationManager configurationManager = getConfigurationManager();
        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(Utils.CDS_CONFIG_RESOURCE_NAME);
        resourceAdd.setAttributes(Collections.singletonList(
                new Attribute(Utils.CDS_CONFIG_ENABLED_ATTRIBUTE, String.valueOf(cdsEnabled))));
        try {
            configurationManager.replaceResource(Utils.CDS_CONFIG_RESOURCE_TYPE, resourceAdd);
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                throw e;
            }
            // The resource type is created on the first update.
            addCDSConfigResourceType(configurationManager);
            configurationManager.replaceResource(Utils.CDS_CONFIG_RESOURCE_TYPE, resourceAdd);
        }
    }

    private void addCDSConfigResourceType(ConfigurationManager configurationManager)
            throws ConfigurationManagementException {

        ResourceTypeAdd resourceType = new ResourceTypeAdd();
        resourceType.setName(Utils.CDS_CONFIG_RESOURCE_TYPE);
        resourceType.setDescription("Resource type for the Customer Data Service configuration.");
        try {
            configurationManager.addResourceType(resourceType);
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS.getCode().equals(e.getErrorCode())) {
                throw e;
            }
        }
    }

    private ConfigurationManager getConfigurationManager() throws CDSClientException {

        ConfigurationManager configurationManager = (ConfigurationManager) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(ConfigurationManager.class, null);
        if (configurationManager == null) {
            throw new CDSClientException("The configuration management service is not available.", false);
        }
        return configurationManager;
    }
}
