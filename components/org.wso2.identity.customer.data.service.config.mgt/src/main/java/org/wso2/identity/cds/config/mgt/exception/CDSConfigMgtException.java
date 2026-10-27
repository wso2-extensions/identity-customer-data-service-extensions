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

package org.wso2.identity.cds.config.mgt.exception;

/**
 * Exception for the CDS config management service.
 */
public class CDSConfigMgtException extends Exception {

    private static final long serialVersionUID = 6826404383927640861L;

    private final boolean clientError;

    /**
     * @param message     Error message.
     * @param clientError true if the request content caused the error.
     */
    public CDSConfigMgtException(String message, boolean clientError) {

        super(message);
        this.clientError = clientError;
    }

    /**
     * @param message Error message.
     * @param cause   Exception to be wrapped.
     */
    public CDSConfigMgtException(String message, Throwable cause) {

        super(message, cause);
        this.clientError = false;
    }

    /**
     * @param message     Error message.
     * @param clientError true if the request content caused the error.
     * @param cause       Exception to be wrapped.
     */
    public CDSConfigMgtException(String message, boolean clientError, Throwable cause) {

        super(message, cause);
        this.clientError = clientError;
    }

    /**
     * @return true if the request content caused the error.
     */
    public boolean isClientError() {

        return clientError;
    }
}
