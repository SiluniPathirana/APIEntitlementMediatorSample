/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

package sample.custom.entitlemet.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.custom.entitlement" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */


public class EntitlementCallbackExtensionComponent {

    private static Log log = LogFactory.getLog(EntitlementCallbackExtensionComponent.class);
    private static RealmService realmService;

    protected void activate(ComponentContext ctxt) {
        try {
            log.info("Entitlement callback extension component activated successfully.");
        } catch (Exception e) {
            log.error("Failed to activate entitlement callback extension component ", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Entitlement callback extension component is deactivated ");
        }
    }

    protected void setRealmService(RealmService realmService) {
        EntitlementCallbackExtensionComponent.realmService = realmService;
        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the entitlement callback extension component");
        }
        EntitlementManagementServiceComponentHolder.getInstance().setRealmService(realmService);

    }

    protected void unsetRealmService(RealmService realmService) {
        EntitlementCallbackExtensionComponent.realmService = null;
        if (log.isDebugEnabled()) {
            log.debug("RealmService is unset in the entitlement callback extension component");
        }
        EntitlementManagementServiceComponentHolder.getInstance().setRealmService(null);

    }

    public static RealmService getRealmService() {
        return EntitlementManagementServiceComponentHolder.getInstance().getRealmService();

    }


}
