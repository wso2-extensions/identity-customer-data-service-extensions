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

/**
 * Exception for a call to the Customer Data Service (CDS) that did not succeed.
 */
public class CDSClientException extends Exception {

    private static final long serialVersionUID = 4373839152387471862L;

    private final boolean clientError;

    /**
     * @param message     Error message.
     * @param clientError true if CDS refused the request because of the request content (HTTP 400).
     */
    public CDSClientException(String message, boolean clientError) {

        super(message);
        this.clientError = clientError;
    }

    public CDSClientException(String message, Throwable cause) {

        super(message, cause);
        this.clientError = false;
    }

    /**
     * @return true if CDS refused the request because of the request content (HTTP 400).
     */
    public boolean isClientError() {

        return clientError;
    }
}
