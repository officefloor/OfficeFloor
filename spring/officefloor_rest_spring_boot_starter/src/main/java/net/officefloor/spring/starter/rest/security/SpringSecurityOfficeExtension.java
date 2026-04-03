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

public class SpringSecurityOfficeExtension implements OfficeExtensionService {

    private OfficeAdministration preAuthorizeAdministration = null;

    private OfficeAdministration postAuthorizeAdministration = null;

    private OfficeAdministration rolesAllowedAdministration = null;

    protected OfficeAdministration getPreAuthorizeAdministration(OfficeArchitect architect) {
        if (this.preAuthorizeAdministration == null) {
            this.preAuthorizeAdministration = architect.addOfficeAdministration(PreAuthorize.class.getSimpleName(),
                    new AuthorizeAdministrationSource<PreAuthorize>(PreAuthorize.class, PreAuthorize::value));
            this.preAuthorizeAdministration.enableAutoWireExtensions();
        }
        return this.preAuthorizeAdministration;
    }

    protected OfficeAdministration getPostAuthorizeAdministration(OfficeArchitect architect) {
        if (this.postAuthorizeAdministration == null) {
            this.postAuthorizeAdministration = architect.addOfficeAdministration(PostAuthorize.class.getSimpleName(),
                    new AuthorizeAdministrationSource<PostAuthorize>(PostAuthorize.class, PostAuthorize::value));
            this.postAuthorizeAdministration.enableAutoWireExtensions();
        }
        return this.postAuthorizeAdministration;
    }

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