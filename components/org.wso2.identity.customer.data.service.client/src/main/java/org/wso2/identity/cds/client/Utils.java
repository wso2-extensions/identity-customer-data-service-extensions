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

package org.wso2.identity.cds.client;

/**
 * This class contains utility methods for the Customer Data Management Service client.
 */
public class Utils {

    public static String getCDMServiceURL() {
        String cdmServiceURL = System.getProperty("cds.service.url");
        if (cdmServiceURL == null || cdmServiceURL.isEmpty()) {
            cdmServiceURL = "http://localhost:8900";
        }
        return cdmServiceURL;
    }

    public static String getBase64EncodedCredentials() {
//        String username = System.getProperty("cds.service.username", "admin");
//        String password = System.getProperty("cds.service.password", "admin");
        String credentials = "admin" + ":" + "admin";
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
