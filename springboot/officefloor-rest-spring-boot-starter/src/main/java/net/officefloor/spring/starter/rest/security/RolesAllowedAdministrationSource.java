/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

/** {@link AbstractAdministrationSource} to enforce roles-allowed security. */
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
