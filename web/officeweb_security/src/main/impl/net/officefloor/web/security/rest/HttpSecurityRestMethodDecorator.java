package net.officefloor.web.security.rest;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.rest.build.RestMethodDecorator;
import net.officefloor.web.rest.build.RestMethodDecoratorContext;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.build.HttpSecurityBuilder;

import java.util.Map;

/**
 * {@link RestMethodDecorator} for the {@link net.officefloor.web.spi.security.HttpSecurity}.
 */
public class HttpSecurityRestMethodDecorator implements RestMethodDecorator<Void> {

    private final Map<String, HttpSecurityBuilder> specificSecurity;

    public HttpSecurityRestMethodDecorator(Map<String, HttpSecurityBuilder> specificSecurity) {
        this.specificSecurity = specificSecurity;
    }

    /*
     * ================ RestMethodDecorator =================
     */

    @Override
    public void decorateRestMethod(RestMethodDecoratorContext<Void> context) {

        // Obtain the security
        String sectionName = "SECURITY_" + context.getHttpMethod().getName() + "_" + context.getPath().getPath();

        // Need to be specific about which security
        HttpSecurityBuilder securityBuilder = this.specificSecurity.get("challenge");

        context.addHttpInputInterceptor((interceptor) -> {

            // Build section to intercept and enforce authentication
            OfficeSection section = interceptor.getOfficeArchitect()
                    .addOfficeSection(sectionName, ClassSectionSource.class.getName(), HttpSecurityAccess.class.getName());

            // Explicitly link the default HttpAccessControl (handles all securities + content-type negotiation)
            OfficeSectionObject accessControlObject = section.getOfficeSectionObject(HttpAccessControl.class.getName());
            interceptor.getOfficeArchitect().link(accessControlObject, securityBuilder.getHttpAccessControl());

            // Link in the section for interception
            OfficeSectionInput input = section.getOfficeSectionInput("service");
            OfficeSectionOutput output = section.getOfficeSectionOutput("output");
            interceptor.link(input, output);
        });
    }

    public static class HttpSecurityAccess {
        public void service(HttpAccessControl accessControl, @Flow("output") Runnable next, ServerHttpConnection connection) {

            // Access allowed
            next.run();
        }
    }

}
