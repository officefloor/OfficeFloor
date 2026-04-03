package net.officefloor.spring.starter.rest.security;


import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandler;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandlerServiceFactory;

/**
 * {@link SpringExceptionHandlerServiceFactory} for Spring Security.
 */
public class SecuritySpringExceptionHandlerServiceFactory implements SpringExceptionHandlerServiceFactory {

    /*
     * ===================== SpringExceptionHandlerServiceFactory =================
     */

    @Override
    public SpringExceptionHandler createService(ServiceContext context) throws Throwable {
        return new SecuritySpringExceptionHandler();
    }

}
