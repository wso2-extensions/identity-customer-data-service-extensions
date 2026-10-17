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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

/**
 * This class contains utility methods for the Customer Data Management Service client.
 */
public class Utils {

    private static final Log LOG = LogFactory.getLog(Utils.class);

    /*
     * The tenant CDS config in the config store. The identity-api-server configs API (/configs/cds) writes this
     * resource. Keep these names the same in both repositories.
     */
    private static final String CDS_CONFIG_RESOURCE_TYPE = "CDS_CONFIGURATION";
    private static final String CDS_CONFIG_RESOURCE_NAME = "cds-config";
    private static final String CDS_CONFIG_ENABLED_ATTRIBUTE = "enabled";

    public static String getCDSServiceURL() {
        return IdentityUtil.getProperty("CustomerDataService.ServerURL");
    }

  // Get Base64 encoded credentials for CDS admin user
    public static String getBase64EncodedCredentials() {

      String usernameProp =
              IdentityUtil.getProperty("CustomerDataService.AdminUsername");
      String passwordProp =
              IdentityUtil.getProperty("CustomerDataService.AdminPassword");

      String resolvedUserName = resolveIfRequired("CustomerDataService.AdminUsername",
                      StringUtils.trimToNull(usernameProp));

      String resolvedPassword = resolveIfRequired("CustomerDataService.AdminPassword",
                      StringUtils.trimToNull(passwordProp));

      if (StringUtils.isBlank(resolvedUserName) || StringUtils.isBlank(resolvedPassword)) {
          return "";
      }

      String credentials = resolvedUserName + ":" + resolvedPassword;

      return Base64.getEncoder()
              .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }


    /**
     * Get the server-level value of the CDS enable config ([customer_data_service] enable).
     *
     * @return true if CDS is enabled for the server.
     */
    public static boolean isCDSEnabled() {
        String cdsEnabled = IdentityUtil.getProperty("CustomerDataService.Enable");
        return Boolean.parseBoolean(cdsEnabled);
    }

    /**
     * Get the effective value of the CDS enable config for a tenant. The tenant value in the config store wins.
     * If the tenant has no value, the server-level value applies. An organization uses the value of its root
     * organization.
     *
     * @param tenantDomain Tenant domain.
     * @return true if CDS is enabled for the tenant.
     */
    public static boolean isCDSEnabled(String tenantDomain) {

        if (StringUtils.isBlank(tenantDomain)) {
            return isCDSEnabled();
        }
        try {
            String rootTenantDomain = tenantDomain;
            if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
                rootTenantDomain = OrganizationManagementUtil.getRootOrgTenantDomainBySubOrgTenantDomain(tenantDomain);
            }
            return getTenantCDSEnabled(rootTenantDomain).orElseGet(Utils::isCDSEnabled);
        } catch (OrganizationManagementException | ConfigurationManagementException e) {
            LOG.warn("Error while reading the CDS config of the tenant: " + sanitizeForLog(tenantDomain) +
                    ". The server-level value is used.", e);
            return isCDSEnabled();
        }
    }

    private static Optional<Boolean> getTenantCDSEnabled(String tenantDomain)
            throws ConfigurationManagementException {

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
            if (isResourceNotFound(e)) {
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

    private static boolean isResourceNotFound(ConfigurationManagementException e) {

        return ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode()
                .equals(e.getErrorCode()) ||
                ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode()
                        .equals(e.getErrorCode());
    }

    // Resolve secrets if secure vault is enabled
    public static String resolveIfRequired(String key, String value) {

        if (value == null) {
            return null;
        }

        Properties props = new Properties();
        props.put(key, value);

        SecretResolver resolver = SecretResolverFactory.create(props);

        if (resolver != null && resolver.isInitialized()) {
            return MiscellaneousUtil.resolve(value, resolver);
        }
        return value;
    }

    /**
     * Prevent CRLF/log forging.
     * Updated to handle more edge cases and satisfy scanners.
     */
    public static String sanitizeForLog(Object input) {
        if (input == null) {
            return "null";
        }
        String clean = String.valueOf(input);
        if (StringUtils.isBlank(clean)) {
            return "empty";
        }
        // Comprehensive CRLF replacement
        return clean.replace("\r", "_")
                .replace("\n", "_")
                .replaceAll("(?i)%0a", "_")
                .replaceAll("(?i)%0d", "_");
    }
}
