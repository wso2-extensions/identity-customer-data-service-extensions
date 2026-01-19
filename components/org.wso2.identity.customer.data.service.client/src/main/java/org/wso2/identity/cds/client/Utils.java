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

/**
 * This class contains utility methods for the Customer Data Management Service client.
 */
public class Utils {

    public static String getCDSServiceURL() {
        return IdentityUtil.getProperty("CustomerDataService.ServerURL");
    }

    public static String getBase64EncodedCredentials() {
        String credentials =  IdentityUtil.getProperty("CustomerDataService.AdminUsername") + ":" +
                IdentityUtil.getProperty("CustomerDataService.AdminPassword");
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    public static boolean isCDSEnabled() {
        String cdsEnabled = IdentityUtil.getProperty("CustomerDataService.Enable");
        return Boolean.parseBoolean(cdsEnabled);
    }

    /**
     * Prevent CRLF/log forging by escaping CR/LF.
     */
    public static String sanitizeForLog(String input) {
        if (StringUtils.isBlank(input)) {
            return "null";
        }
        return input.replace("\r", "\\r").replace("\n", "\\n");
    }

    /**
     * Clip long strings to keep logs safe and readable.
     */
    public static String clip(String input, int maxChars) {
        if (input == null) {
            return null;
        }
        if (maxChars <= 0 || input.length() <= maxChars) {
            return input;
        }
        return input.substring(0, maxChars) + "...(clipped)";
    }
}
