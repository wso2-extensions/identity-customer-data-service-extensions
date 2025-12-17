package org.wso2.identity.cds.event.handler.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.identity.cds.event.handler.ClaimEventHandler;
import org.wso2.identity.cds.event.handler.IdentityEventHandler;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;

/**
 * OSGi component that registers the CDS IdentifyEventHandler.
 */
@Component(name = "org.wso2.identity.cds.event.handler", immediate = true)
public class IdentifyEventHandlerComponent {

    private static final Log log = LogFactory.getLog(IdentifyEventHandlerComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {

        IdentityEventHandler identityEventHandler = new IdentityEventHandler();
        ctx.getBundleContext().registerService(AbstractEventHandler.class.getName(), identityEventHandler, null);
        ClaimEventHandler claimEventHandler = new ClaimEventHandler();
        ctx.getBundleContext().registerService(AbstractEventHandler.class.getName(), claimEventHandler, null);
        log.info("CDS EventHandlers activated successfully.");
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {

        if (log.isDebugEnabled()) {
            log.debug("CDS EventHandlers deactivated.");
        }
    }
}
