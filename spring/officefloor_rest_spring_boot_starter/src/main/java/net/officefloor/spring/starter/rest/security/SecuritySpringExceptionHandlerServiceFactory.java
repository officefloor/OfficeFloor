package net.officefloor.spring.starter.rest.security;


import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.starter.rest.SpringBootExtra;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandler;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandlerServiceFactory;

/**
 * {@link SpringExceptionHandlerServiceFactory} for Spring Security.
 */
public class SecuritySpringExceptionHandlerServiceFactory implements SpringExceptionHandlerServiceFactory, SpringExceptionHandler {

    /**
     * May not be on class path, so must dynamically load.
     */
    public static final String SPRING_SECURITY_EXCEPTION_HANDLER_CLASS_NAME = "net.officefloor.spring.starter.rest.security.SecuritySpringExceptionHandler";

    /*
     * ===================== SpringExceptionHandlerServiceFactory =================
     */

    @Override
    public SpringExceptionHandler createService(ServiceContext context) throws Throwable {
        return SpringBootExtra.loadService(SpringSecurityOfficeExtensionServiceFactory.SPRING_SECURITY_FILTER_CLASS_NAME,
                SPRING_SECURITY_EXCEPTION_HANDLER_CLASS_NAME, context, this);
    }

    /*
     * ============================ SpringExceptionHandler =========================
     */

    @Override
    public boolean handle(Throwable exception, SpringServerHttpConnection connection) throws Exception {
        return false; // not handled
    }
}
