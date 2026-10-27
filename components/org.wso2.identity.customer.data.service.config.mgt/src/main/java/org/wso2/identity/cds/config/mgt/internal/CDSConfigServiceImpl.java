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

package org.wso2.identity.cds.config.mgt.internal;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.identity.cds.client.CDSClient;
import org.wso2.identity.cds.client.CDSClientException;
import org.wso2.identity.cds.client.Utils;
import org.wso2.identity.cds.config.mgt.CDSConfigService;
import org.wso2.identity.cds.config.mgt.exception.CDSConfigMgtException;

import java.util.Collections;
import java.util.Optional;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS;

/**
 * Implementation of {@link CDSConfigService}. The value is in the config store: resource type CDS_CONFIGURATION,
 * resource cds-config, attribute enabled.
 */
public class CDSConfigServiceImpl implements CDSConfigService {

    private static final String CDS_CONFIG_RESOURCE_TYPE = "CDS_CONFIGURATION";
    private static final String CDS_CONFIG_RESOURCE_NAME = "cds-config";
    private static final String CDS_CONFIG_ENABLED_ATTRIBUTE = "enabled";

    @Override
    public boolean isCDSEnabled(String tenantDomain) throws CDSConfigMgtException {

        if (!Utils.isCDSEnabled() || StringUtils.isBlank(tenantDomain)) {
            return false;
        }
        try {
            return getTenantCDSEnabled(getRootTenantDomain(tenantDomain)).orElse(false);
        } catch (OrganizationManagementException | ConfigurationManagementException e) {
            throw new CDSConfigMgtException("Error while reading the CDS config of the tenant: " +
                    Utils.sanitizeForLog(tenantDomain), e);
        }
    }

    @Override
    public void updateCDSEnabled(String tenantDomain, boolean cdsEnabled) throws CDSConfigMgtException {

        if (!Utils.isCDSEnabled()) {
            throw new CDSConfigMgtException("CDS is not enabled for the server.", true);
        }
        // CDS gets the value first. If CDS does not accept it, the value is not stored.
        try {
            CDSClient.updateCDSConfig(tenantDomain, cdsEnabled);
        } catch (CDSClientException e) {
            throw new CDSConfigMgtException(e.getMessage(), e.isClientError(), e);
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain);
            carbonContext.setTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
            storeCDSEnabled(cdsEnabled);
        } catch (ConfigurationManagementException e) {
            throw new CDSConfigMgtException("Error while storing the CDS config of the tenant: " +
                    Utils.sanitizeForLog(tenantDomain), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Get the tenant domain of the root organization. A root organization gets its own tenant domain.
     */
    private String getRootTenantDomain(String tenantDomain) throws OrganizationManagementException {

        if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
            return OrganizationManagementUtil.getRootOrgTenantDomainBySubOrgTenantDomain(tenantDomain);
        }
        return tenantDomain;
    }

    private Optional<Boolean> getTenantCDSEnabled(String tenantDomain) throws ConfigurationManagementException {

        ConfigurationManager configurationManager = (ConfigurationManager) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(ConfigurationManager.class, null);
        if (configurationManager == null) {
            return Optional.empty();
        }
        Resource resource;
        try {
            resource = configurationManager.getResourceByTenantId(IdentityTenantUtil.getTenantId(tenantDomain),
                    CDS_CONFIG_RESOURCE_TYPE, CDS_CONFIG_RESOURCE_NAME);
        } catch (ConfigurationManagementException e) {
            if (ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) ||
                    ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                return Optional.empty();
            }
            throw e;
        }
        if (resource == null || resource.getAttributes() == null) {
            return Optional.empty();
        }
        for (Attribute attribute : resource.getAttributes()) {
            if (CDS_CONFIG_ENABLED_ATTRIBUTE.equals(attribute.getKey())) {
                return Optional.of(Boolean.parseBoolean(attribute.getValue()));
            }
        }
        return Optional.empty();
    }

    private void storeCDSEnabled(boolean cdsEnabled) throws ConfigurationManagementException, CDSConfigMgtException {

        ConfigurationManager configurationManager = getConfigurationManager();
        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(CDS_CONFIG_RESOURCE_NAME);
        resourceAdd.setAttributes(Collections.singletonList(
                new Attribute(CDS_CONFIG_ENABLED_ATTRIBUTE, String.valueOf(cdsEnabled))));
        try {
            configurationManager.replaceResource(CDS_CONFIG_RESOURCE_TYPE, resourceAdd);
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                throw e;
            }
            // The resource type is created on the first update.
            addCDSConfigResourceType(configurationManager);
            configurationManager.replaceResource(CDS_CONFIG_RESOURCE_TYPE, resourceAdd);
        }
    }

    private void addCDSConfigResourceType(ConfigurationManager configurationManager)
            throws ConfigurationManagementException {

        ResourceTypeAdd resourceType = new ResourceTypeAdd();
        resourceType.setName(CDS_CONFIG_RESOURCE_TYPE);
        resourceType.setDescription("Resource type for the Customer Data Service configuration.");
        try {
            configurationManager.addResourceType(resourceType);
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS.getCode().equals(e.getErrorCode())) {
                throw e;
            }
        }
    }

    private ConfigurationManager getConfigurationManager() throws CDSConfigMgtException {

        ConfigurationManager configurationManager = (ConfigurationManager) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(ConfigurationManager.class, null);
        if (configurationManager == null) {
            throw new CDSConfigMgtException("The configuration management service is not available.", false);
        }
        return configurationManager;
    }
}
