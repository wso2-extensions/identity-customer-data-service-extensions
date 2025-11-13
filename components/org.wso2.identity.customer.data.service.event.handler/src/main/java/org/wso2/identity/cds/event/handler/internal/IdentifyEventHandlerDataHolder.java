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

package org.wso2.identity.cds.event.handler.internal;

/**
 * Data holder for the CDS Identify Event Handler bundle.
 * Used to store OSGi service references if needed in the future.
 */
public class IdentifyEventHandlerDataHolder {

    private static final IdentifyEventHandlerDataHolder instance = new IdentifyEventHandlerDataHolder();

    private IdentifyEventHandlerDataHolder() {
    }

    public static IdentifyEventHandlerDataHolder getInstance() {
        return instance;
    }


}
