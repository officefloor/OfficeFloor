package net.officefloor.spring.starter.rest.security;

import jakarta.annotation.security.RolesAllowed;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

public class RolesAllowedAdministrationSource extends AbstractAdministrationSource<ServerHttpConnection, None, None> implements
        AdministrationFactory<ServerHttpConnection, None, None>, Administration<ServerHttpConnection, None, None> {

    /*
     * ===================== AdministrationSource ======================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<ServerHttpConnection, None, None> context) throws Exception {
        context.setExtensionInterface(ServerHttpConnection.class);
        context.setAdministrationFactory(this);
    }

    /*
     * ===================== AdministrationFactory ======================
     */

    @Override
    public Administration<ServerHttpConnection, None, None> createAdministration() throws Throwable {
        return this;
    }

    /*
     * ========================= Administration ==========================
     */

    @Override
    public void administer(AdministrationContext<ServerHttpConnection, None, None> context) throws Throwable {

        // Obtain the authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Determine if roles allowed
        for (Object annotation : context.getManagedFunctionAnnotations()) {

            // Potentially obtain roles
            String[] roles = null;
            if (annotation instanceof Secured) {
                Secured secured = (Secured) annotation;
                roles = secured.value();
            } else if (annotation instanceof RolesAllowed) {
                RolesAllowed rolesAllowed = (RolesAllowed) annotation;
                roles = rolesAllowed.value();

                // Prefix the roles
                for (int i = 0; i < roles.length; i++) {
                    roles[i] = "ROLE_" + roles[i];
                }
            }

            // If have roles, undertake authorization
            if (roles != null) {
                boolean isPermitted = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(Arrays.asList(roles)::contains);
                if (!isPermitted) {
                    throw new AccessDeniedException("Access Denied");
                }
            }
        }
    }

}