package net.officefloor.spring.starter.rest.security;

import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

public class SpringSecurityOfficeExtensionServiceFactory implements OfficeExtensionServiceFactory {

    /*
     * ================== OfficeExtensionServiceFactory ================
     */

    @Override
    public OfficeExtensionService createService(ServiceContext context) throws Throwable {
        return new SpringSecurityOfficeExtension();
    }
}
