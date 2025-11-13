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

package org.wso2.identity.cds.auth.handler;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CDSPostAuthnHandler extends AbstractPostAuthnHandler {

    public static final String PROFILE_ID = "profileId";
    public static final String CDS_PROFILE_ID_COOKIE = "cds_profile_id";

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request,
                                             HttpServletResponse response,
                                             AuthenticationContext context)
            throws PostAuthenticationFailedException {

        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if (CDS_PROFILE_ID_COOKIE.equals(cookie.getName())) {
                    String val = cookie.getValue();
                    context.setProperty(PROFILE_ID, val);
                    break;
                }
            }
        }
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }
}
