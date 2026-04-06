package net.officefloor.spring.starter.rest;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Convenience {@link OfficeFloorSpringBootExtensionServiceFactory} that handles extra Spring Boots that may not be on the class path.
 */
public abstract class AbstractExtraSpringBootExtensionServiceFactory implements OfficeFloorSpringBootExtensionServiceFactory, OfficeFloorSpringBootExtension {

    /**
     * Specifies the {@link Class} that would be on path if extra Spring Boot included.
     *
     * @return {@link Class} name.
     */
    public abstract String getExtraKeyClassName();

    /**
     * Name of the {@link OfficeFloorSpringBootExtension} {@link Class}.
     *
     * @return {@link OfficeFloorSpringBootExtension} {@link Class}.
     */
    public abstract String getOfficeFloorSpringBootExtensionClassName();

    /*
     * ==================== OfficeFloorSpringBootExtensionServiceFactory ================
     */

    @Override
    public OfficeFloorSpringBootExtension createService(ServiceContext context) throws Throwable {
        return SpringBootExtra.loadService(this.getExtraKeyClassName(), this.getOfficeFloorSpringBootExtensionClassName(), context, this);
    }

    /*
     * =========================== OfficeFloorSpringBootExtension ========================
     */

    @Override
    public void extendSpringBootSupport(OfficeFloorSpringBootExtensionContext context) throws Exception {
        // Not on class path, so no extra support
    }
}
