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

package org.wso2.identity.cds.config.mgt;

import org.wso2.identity.cds.config.mgt.exception.CDSConfigMgtException;
import org.wso2.identity.cds.config.mgt.model.CDSConfig;

/**
 * OSGi service for the Customer Data Service (CDS) configuration of a tenant. The identity-api-server configs API
 * (/configs/cds) and the CDS event handlers use this service.
 */
public interface CDSConfigService {

    /**
     * Get the CDS configuration of a tenant. CDS is enabled for a tenant only if the CDS feature is enabled and the
     * tenant enabled it. An organization uses the configuration of its root organization.
     *
     * @param tenantDomain Tenant domain.
     * @return CDS configuration of the tenant.
     * @throws CDSConfigMgtException If the configuration cannot be read.
     */
    CDSConfig getCDSConfig(String tenantDomain) throws CDSConfigMgtException;

    /**
     * Update the CDS configuration of a root tenant. The configuration goes to CDS first. If CDS does not accept
     * it, the configuration is not stored.
     *
     * @param cdsConfig    CDS configuration.
     * @param tenantDomain Tenant domain of a root organization.
     * @throws CDSConfigMgtException If the CDS feature is not enabled, if CDS does not accept the configuration, or if
     *                               the configuration cannot be stored.
     */
    void updateCDSConfig(CDSConfig cdsConfig, String tenantDomain) throws CDSConfigMgtException;
}
