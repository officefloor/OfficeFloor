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
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.frame.api.manage.OfficeFloor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/** {@link OfficeExtensionService} for Spring Security. */
public class SpringSecurityOfficeExtension implements OfficeExtensionService {

    private OfficeAdministration preAuthorizeAdministration = null;

    private OfficeAdministration postAuthorizeAdministration = null;

    private OfficeAdministration rolesAllowedAdministration = null;

    /**
     * @param architect {@link OfficeArchitect}.
     * @return {@link OfficeAdministration} for {@link PreAuthorize}.
     */
    protected OfficeAdministration getPreAuthorizeAdministration(OfficeArchitect architect) {
        if (this.preAuthorizeAdministration == null) {
            this.preAuthorizeAdministration = architect.addOfficeAdministration(PreAuthorize.class.getSimpleName(),
                    new AuthorizeAdministrationSource<PreAuthorize>(PreAuthorize.class, PreAuthorize::value));
            this.preAuthorizeAdministration.enableAutoWireExtensions();
        }
        return this.preAuthorizeAdministration;
    }

    /**
     * @param architect {@link OfficeArchitect}.
     * @return {@link OfficeAdministration} for {@link PostAuthorize}.
     */
    protected OfficeAdministration getPostAuthorizeAdministration(OfficeArchitect architect) {
        if (this.postAuthorizeAdministration == null) {
            this.postAuthorizeAdministration = architect.addOfficeAdministration(PostAuthorize.class.getSimpleName(),
                    new AuthorizeAdministrationSource<PostAuthorize>(PostAuthorize.class, PostAuthorize::value));
            this.postAuthorizeAdministration.enableAutoWireExtensions();
        }
        return this.postAuthorizeAdministration;
    }

    /**
     * @param architect {@link OfficeArchitect}.
     * @return {@link OfficeAdministration} for roles allowed.
     */
    protected OfficeAdministration getRolesAllowedAdministration(OfficeArchitect architect) {
        if (this.rolesAllowedAdministration == null) {
            this.rolesAllowedAdministration = architect.addOfficeAdministration(RolesAllowed.class.getSimpleName(),
                    new RolesAllowedAdministrationSource());
            this.rolesAllowedAdministration.enableAutoWireExtensions();
        }
        return this.rolesAllowedAdministration;
    }

    /*
     * ===================== OfficeExtensionService ====================
     */

    @Override
    public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext officeContext) throws Exception {

        // Flag that bridging Spring security
        officeContext.getLogger().info(OfficeFloor.class.getSimpleName() + " bridging Spring Security");

        // Add administration for the managed functions
        officeArchitect.addManagedFunctionAugmentor((augmentContext) -> {
            ManagedFunctionType<?, ?> managedFunctionType = augmentContext.getManagedFunctionType();

            // PreAuthorize
            PreAuthorize preAuthorize = managedFunctionType.getAnnotation(PreAuthorize.class);
            if (preAuthorize != null) {
                augmentContext.addPreAdministration(this.getPreAuthorizeAdministration(officeArchitect));
            }

            // PostAuthorize
            PostAuthorize postAuthorize = managedFunctionType.getAnnotation(PostAuthorize.class);
            if (postAuthorize != null) {
                augmentContext.addPostAdministration(this.getPostAuthorizeAdministration(officeArchitect));
            }

            // RolesAllowed
            RolesAllowed rolesAllowed = managedFunctionType.getAnnotation(RolesAllowed.class);
            if (rolesAllowed != null) {
                augmentContext.addPreAdministration(this.getRolesAllowedAdministration(officeArchitect));
            }

            // Secured
            Secured secured = managedFunctionType.getAnnotation(Secured.class);
            if (secured != null) {
                augmentContext.addPreAdministration(this.getRolesAllowedAdministration(officeArchitect));
            }
        });
    }

}
