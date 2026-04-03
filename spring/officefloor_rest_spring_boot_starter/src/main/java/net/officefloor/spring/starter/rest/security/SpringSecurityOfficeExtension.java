package net.officefloor.spring.starter.rest.security;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.frame.api.manage.OfficeFloor;
import org.springframework.security.access.prepost.PreAuthorize;

public class SpringSecurityOfficeExtension implements OfficeExtensionService {

    private OfficeAdministration preAuthorizeAdministration = null;

    protected OfficeAdministration getPreAuthorizeAdministration(OfficeArchitect architect) {
        if (this.preAuthorizeAdministration == null) {
            this.preAuthorizeAdministration = architect.addOfficeAdministration(PreAuthorize.class.getSimpleName(), new AuthorizeAdministrationSource<PreAuthorize>(PreAuthorize.class, PreAuthorize::value));

        }
        return this.preAuthorizeAdministration;
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

        });
    }

}