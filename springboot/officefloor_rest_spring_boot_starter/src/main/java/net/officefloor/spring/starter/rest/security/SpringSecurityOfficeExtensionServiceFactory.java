package net.officefloor.spring.starter.rest.security;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.starter.rest.SpringBootExtra;

public class SpringSecurityOfficeExtensionServiceFactory implements OfficeExtensionServiceFactory, OfficeExtensionService {

    /**
     * May not be on class path, so must dynamically load.
     */
    public static final String SPRING_SECURITY_FILTER_CLASS_NAME = "org.springframework.security.web.SecurityFilterChain";
    public static final String SPRING_SECURITY_OFFICE_EXTENSION_CLASS_NAME = "net.officefloor.spring.starter.rest.security.SpringSecurityOfficeExtension";

    /*
     * ================== OfficeExtensionServiceFactory ================
     */

    @Override
    public OfficeExtensionService createService(ServiceContext context) throws Throwable {
        return SpringBootExtra.loadService(SPRING_SECURITY_FILTER_CLASS_NAME,
                SPRING_SECURITY_OFFICE_EXTENSION_CLASS_NAME, context, this);
    }

    /*
     * ======================== OfficeExtension =========================
     */

    @Override
    public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
        // No Spring Security, so nothing to extend
    }

}
