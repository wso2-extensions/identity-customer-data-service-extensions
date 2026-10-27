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

/**
 * OSGi service for the tenant CDS enable config. The identity-api-server configs API (/configs/cds) and the CDS
 * event handlers use this service.
 */
public interface CDSConfigService {

    /**
     * Get the effective value of the CDS enable config for a tenant. CDS is enabled for a tenant only if it is
     * enabled for the server and the tenant value is true. If the tenant has no value, CDS is not enabled for the
     * tenant. An organization gets the value of its root organization.
     *
     * @param tenantDomain Tenant domain.
     * @return true if CDS is enabled for the tenant.
     * @throws CDSConfigMgtException If the value cannot be read.
     */
    boolean isCDSEnabled(String tenantDomain) throws CDSConfigMgtException;

    /**
     * Update the CDS enable config of a root tenant. The value goes to CDS first. If CDS does not accept the
     * value, the value is not stored.
     *
     * @param tenantDomain Tenant domain of a root organization.
     * @param cdsEnabled   The new value.
     * @throws CDSConfigMgtException If CDS is not enabled for the server, if CDS does not accept the value, or if
     *                               the value cannot be stored. {@link CDSConfigMgtException#isClientError()} is
     *                               true in the first two cases.
     */
    void updateCDSEnabled(String tenantDomain, boolean cdsEnabled) throws CDSConfigMgtException;
}
