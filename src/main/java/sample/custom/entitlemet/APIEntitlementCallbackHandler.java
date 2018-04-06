
/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package sample.custom.entitlemet;


import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;

import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.mediator.callback.EntitlementCallbackHandler;
import org.wso2.carbon.identity.entitlement.proxy.Attribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import sample.custom.entitlemet.internal.EntitlementCallbackExtensionComponent;


public class APIEntitlementCallbackHandler extends EntitlementCallbackHandler {
    private static final Log log = LogFactory.getLog(APIEntitlementCallbackHandler.class);
    private static final String CLAIM_URI = "http://wso2.org/claims/tcCodes";
    private static final String CUSTOM_SEPARATOR = "CustomSeparator";
    public static final String APPLICATION_ATTRIBUTE_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:custom";
    public static final String APPLICATION_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:application:application-name";
    public static final String ATTRIBUTE_DATA_TYPE = "string";
    public static final String CLAIM_ATTRIBUTE_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    public static final String CLAIM_ATTRIBUTE_ID = "http://wso2.org/claims/tcCodes";

    public String getUserName(MessageContext synCtx) {
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        String userName = null;
        if (authContext != null) {
            userName = authContext.getUsername();

        }
        log.debug("UserName ---" + userName);
        return userName;
    }

    public String findServiceName(MessageContext synCtx) {
        String path = ((String) synCtx.getProperty(RESTConstants.REST_SUB_REQUEST_PATH));
        log.debug("SERVICE - REST SUB REQUEST ---" + path);
        return path;

    }

    public String findAction(MessageContext synCtx) {

        log.debug("Operation Name ---" + (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().getProperty(org.apache.axis2.Constants.Configuration.HTTP_METHOD));
        return (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().getProperty(org.apache.axis2.Constants.Configuration.HTTP_METHOD);

    }

    public String findOperationName(MessageContext synCtx) {
        return null;
    }

    public String[] findEnvironment(MessageContext synCtx) {
        return null;
    }


    @Override
    public Attribute[] findOtherAttributes(MessageContext synCtx) {
        log.debug("Start Method findOtherAttributes ");


        try {
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
            String[] values = getTermsAndConditionsClaimValues(authContext);
            if (values.length > 0) {

                int count = values.length;
                if (log.isDebugEnabled()) {
                    log.debug("No of claim values " + count);
                }

                Attribute[] attributes = new Attribute[count + 1];
                Attribute attributeId = new Attribute(APPLICATION_ATTRIBUTE_CATEGORY,
                        APPLICATION_ATTRIBUTE_ID, ATTRIBUTE_DATA_TYPE, authContext.getApplicationName());
                attributes[0] = attributeId;

                for (int i = 1; i <= count; i++) {
                    attributes[i] = new Attribute(CLAIM_ATTRIBUTE_CATEGORY,
                            CLAIM_ATTRIBUTE_ID, ATTRIBUTE_DATA_TYPE, values[i - 1]);
                    if (log.isDebugEnabled()) {
                        log.debug(" Attribute values " + attributes[i]);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Finished Method findOtherAttributes ");
                }
                return attributes;
            } else {
                return null;
            }

        } catch (UserStoreException e) {
            String errorMessage = "Error while accessing user store";
            log.error(errorMessage, e);
            throw new SynapseException(errorMessage, e);
        }


    }

    private String[] getTermsAndConditionsClaimValues(AuthenticationContext authContext) throws UserStoreException {
        String[] values = new String[0];
        UserStoreManager userStoreManager = EntitlementCallbackExtensionComponent.getRealmService().getTenantUserRealm(
                CarbonContext.getThreadLocalCarbonContext().getTenantId()).getUserStoreManager();
        String subscriber = authContext.getSubscriber();
        if (userStoreManager != null) {
            String userClaimValue = userStoreManager.getUserClaimValue(subscriber, CLAIM_URI, subscriber);
            if (userClaimValue != null) {
                String separator = EntitlementCallbackExtensionComponent.getRealmService()
                        .getBootstrapRealmConfiguration().getUserStoreProperties().get(CUSTOM_SEPARATOR);
                if (log.isDebugEnabled()) {
                    log.debug("User Claim values : " + userClaimValue);
                }
                values = userClaimValue.split(separator);
            }
        }
        return values;
    }

}