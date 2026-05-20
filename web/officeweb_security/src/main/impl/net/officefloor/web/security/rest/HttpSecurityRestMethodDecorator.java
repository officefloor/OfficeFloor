package net.officefloor.web.security.rest;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.rest.build.RestMethodDecorator;
import net.officefloor.web.rest.build.RestMethodDecoratorContext;
import net.officefloor.web.rest.build.RestPathContext;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.build.rest.HttpAccessConfiguration;
import net.officefloor.web.security.build.rest.HttpSecurityConfiguration;
import net.officefloor.web.spi.security.HttpSecurity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // Obtain the security section name
        String sectionName = "SECURITY_" + context.getHttpMethod().getName() + "_" + context.getPath().getPath();

        // Obtain the security configurations
        List<HttpSecurityConfiguration> configurations = new LinkedList<>();
        HttpSecurityConfiguration configuration = context.getConfiguration("security", HttpSecurityConfiguration.class);
        if (configuration != null) {
            configurations.add(configuration);
        }
        RestPathContext path = context.getPath();
        do {
            configuration = path.getConfiguration("security", HttpSecurityConfiguration.class);
            if (configuration != null) {
                configurations.add(configuration);
            }
            path = path.getParentPath();
        } while (path != null);

        // Determine if security configured
        if (configurations.isEmpty()) {
            return; // not secured
        }

        // Lowest configuration indicates the HTTP Security instances involved
        configuration = configurations.get(0);

        // Obtain the architect
        OfficeArchitect officeArchitect = context.getOfficeArchitect();

        // Obtain the access
        List<AccessBuilder> accessBuilders = new LinkedList<>();
        for (String securityName : configuration.getAccesses().keySet()) {

            // Obtain the security builder
            HttpSecurityBuilder securityBuilder = this.securities.get(securityName);
            if (securityBuilder == null) {
                throw officeArchitect.addIssue("No " + HttpSecurity.class.getSimpleName() + " configured by name '" + securityName + "'");
            }
            OfficeManagedObject accessControlManagedObject = securityBuilder.getHttpAccessControl();

            // any-roles takes the lowest configuration only
            String[] anyRoles = new String[] {};
            FOUND_ANY_ROLES: for (HttpSecurityConfiguration securityConfig : configurations) {
                HttpAccessConfiguration accessConfig = securityConfig.getAccesses().get(securityName);
                if (accessConfig != null) {
                    List<String> roles = accessConfig.getAnyRole();
                    if (roles != null) {
                        anyRoles = roles.toArray(String[]::new);
                        break FOUND_ANY_ROLES;
                    }
                }
            }

            // all-roles inherits roles (unless overridden by inherit-all-roles: false)
            Set<String> allRoles = new HashSet<>();
            ALL_ROLES_DONE: for (HttpSecurityConfiguration securityConfig : configurations) {
                HttpAccessConfiguration accessConfig = securityConfig.getAccesses().get(securityName);
                if (accessConfig != null) {
                    List<String> roles = accessConfig.getAllRoles();
                    if (roles != null) {
                        allRoles.addAll(roles);
                    }
                    if (Boolean.FALSE.equals(accessConfig.getInheritAllRoles())) {
                        break ALL_ROLES_DONE;
                    }
                }
            }

            // Add the access builder
            int accessIndex = accessBuilders.size();
            accessBuilders.add(new AccessBuilder(accessControlManagedObject, new Access(accessIndex, anyRoles, allRoles.toArray(String[]::new))));
        }

        // Create the Accesses
        Access[] accesses = accessBuilders.stream()
                .map((builder) -> builder.access)
                .toArray(Access[]::new);

        context.addHttpInputInterceptor((interceptor) -> {

            // Build section to intercept and enforce authentication
            OfficeSection section = interceptor.getOfficeArchitect()
                    .addOfficeSection(sectionName, new HttpSecurityAccessSectionSource(accesses), null);

            // Link each of the accesses
            for (int i = 0; i < accessBuilders.size(); i++) {
                AccessBuilder accessBuilder = accessBuilders.get(i);
                OfficeSectionObject accessControlObject = section.getOfficeSectionObject(HttpAccessControl.class.getSimpleName() + i);
                interceptor.getOfficeArchitect().link(accessControlObject, accessBuilder.httpAccessControlManagedObject);
            }

            // Link in the section for interception
            OfficeSectionInput input = section.getOfficeSectionInput("input");
            OfficeSectionOutput output = section.getOfficeSectionOutput("output");
            interceptor.link(input, output);
        });
    }

    private static class AccessBuilder {

        private final OfficeManagedObject httpAccessControlManagedObject;

        private final Access access;

        private AccessBuilder(OfficeManagedObject httpAccessControlManagedObject, Access access) {
            this.httpAccessControlManagedObject = httpAccessControlManagedObject;
            this.access = access;
        }
    }

    private static class Access {

        private final int managedObjectIndex;

        private final String[] anyRoles;

        private final String[] allRoles;

        private Access(int managedObjectIndex, String[] anyRoles, String[] allRoles) {
            this.managedObjectIndex = managedObjectIndex;
            this.anyRoles = anyRoles;
            this.allRoles = allRoles;
        }
    }

    private static class HttpSecurityAccessSectionSource extends AbstractSectionSource {

        private final Access[] accesses;

        public HttpSecurityAccessSectionSource(Access[] accesses) {
            this.accesses = accesses;
        }

        @Override
        protected void loadSpecification(SpecificationContext context) {
            // No specification
        }

        @Override
        public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

            // Add the managed function for handling access
            SectionFunction function = designer.addSectionFunctionNamespace("Access", new HttpSecurityAccessManagedObjectSource(this.accesses))
                    .addSectionFunction("Access", "Access");

            // Link flows
            designer.link(designer.addSectionInput("input", null), function);
            designer.link(function.getFunctionFlow(HttpSecurityAccessFlow.SECURE.name()),
                    designer.addSectionOutput("output", null, false),
                    false);

            // Link objects
            designer.link(function.getFunctionObject("0"), designer.addSectionObject(ServerHttpConnection.class.getSimpleName(), ServerHttpConnection.class.getName()));
            for (int i = 0; i < this.accesses.length; i++) {
                designer.link(function.getFunctionObject(String.valueOf(i + 1)), designer.addSectionObject(HttpAccessControl.class.getSimpleName() + i, HttpAccessControl.class.getName()));
            }
        }
    }

    private static enum HttpSecurityAccessFlow {
        SECURE
    }

    private static class HttpSecurityAccessManagedObjectSource extends AbstractManagedFunctionSource
            implements ManagedFunctionFactory<Indexed, HttpSecurityAccessFlow>, ManagedFunction<Indexed, HttpSecurityAccessFlow> {

        private final Access[] accesses;

        public HttpSecurityAccessManagedObjectSource(Access[] accesses) {
            this.accesses = accesses;
        }

        @Override
        protected void loadSpecification(SpecificationContext context) {
            // No specification
        }

        @Override
        public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder, ManagedFunctionSourceContext context) throws Exception {

            // Add the function
            ManagedFunctionTypeBuilder<Indexed, HttpSecurityAccessFlow> function = functionNamespaceTypeBuilder.addManagedFunctionType("Access", Indexed.class, HttpSecurityAccessFlow.class);
            function.setFunctionFactory(this);
            function.addFlow().setKey(HttpSecurityAccessFlow.SECURE);

            // Add the dependencies
            function.addObject(ServerHttpConnection.class);

            // Add the access controls
            for (Access access : this.accesses) {
                function.addObject(HttpAccessControl.class);
            }
        }

        @Override
        public ManagedFunction<Indexed, HttpSecurityAccessFlow> createManagedFunction() throws Throwable {
            return this;
        }

        @Override
        public void execute(ManagedFunctionContext<Indexed, HttpSecurityAccessFlow> context) throws Throwable {

            // Determine if have access
            for (int i = 0; i < this.accesses.length; i++) {
                Access access = this.accesses[i];

                // Obtain the access
                HttpAccessControl accessControl = (HttpAccessControl) context.getObject(i + 1);
                if (accessControl.isAccess(access.anyRoles, access.allRoles)) {

                    // Secure, so allow through
                    context.doFlow(HttpSecurityAccessFlow.SECURE, null, null);
                    return; // secure and serviced
                }
            }

            // As here, no access
            ServerHttpConnection connection = (ServerHttpConnection) context.getObject(0);
            connection.getResponse().setStatus(HttpStatus.FORBIDDEN);
        }
    }

}
