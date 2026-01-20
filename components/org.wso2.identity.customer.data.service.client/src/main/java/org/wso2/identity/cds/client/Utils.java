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
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This class contains utility methods for the Customer Data Management Service client.
 */
public class Utils {

    public static String getCDSServiceURL() {
        return IdentityUtil.getProperty("CustomerDataService.ServerURL");
    }

  // Get Base64 encoded credentials for CDS admin user
    public static String getBase64EncodedCredentials() {
        String username = IdentityUtil.getProperty("CustomerDataService.AdminUsername");
        String password = IdentityUtil.getProperty("CustomerDataService.AdminPassword");

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return "";
        }

        String credentials = username + ":" + password;
        // Explicitly use UTF_8 to prevent reliance on default system encoding
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isCDSEnabled() {
        String cdsEnabled = IdentityUtil.getProperty("CustomerDataService.Enable");
        return Boolean.parseBoolean(cdsEnabled);
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
                .replace("%0a", "_")
                .replace("%0d", "_");
    }

}
