package net.officefloor.web.security.rest;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
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

    private final Map<String, HttpSecurityBuilder> securities;

    public HttpSecurityRestMethodDecorator(Map<String, HttpSecurityBuilder> securities) {
        this.securities = securities;
    }

    /*
     * ================ RestMethodDecorator =================
     */

    @Override
    public void decorateRestMethod(RestMethodDecoratorContext<Void> context) {

        // TODO handle different securities
        HttpSecurityBuilder securityBuilder = this.securities.get("one");

        context.addHttpInputInterceptor((interceptor) -> {

            OfficeSection section = interceptor.getOfficeArchitect()
                    .addOfficeSection("Access", ClassSectionSource.class.getName(), HttpSecurityAccess.class.getName());

            // Link in the section for interception
            OfficeSectionInput input = section.getOfficeSectionInput("service");
            OfficeSectionOutput output = section.getOfficeSectionOutput("output");
            interceptor.link(input, output);
        });
    }

    public static class HttpSecurityAccess {
        public void service(HttpAccessControl accessControl, ServerHttpConnection connection, @Flow("output") Runnable accessible) throws Exception {
            connection.getResponse().getEntityWriter().write("Access");
        }
    }

}
