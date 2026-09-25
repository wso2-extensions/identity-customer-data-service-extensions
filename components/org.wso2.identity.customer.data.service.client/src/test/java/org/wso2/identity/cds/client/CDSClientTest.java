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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link CDSClient}.
 */
public class CDSClientTest {

    @DataProvider(name = "cdsResponses")
    public Object[][] cdsResponses() {

        return new Object[][]{
                // CDS is not enabled for the organization. Expected response, not a sync failure.
                {400, "CDS-16001", true},
                // Other client and server errors are genuine sync failures.
                {400, "CDS-16002", false},
                {401, "CDS-16001", false},
                {500, "CDS-15001", false},
                {400, null, false}
        };
    }

    @Test(dataProvider = "cdsResponses")
    public void testIsCdsNotEnabled(int statusCode, String errorCode, boolean expected) {

        assertEquals(CDSClient.isCdsNotEnabled(statusCode, errorCode), expected);
    }
}
