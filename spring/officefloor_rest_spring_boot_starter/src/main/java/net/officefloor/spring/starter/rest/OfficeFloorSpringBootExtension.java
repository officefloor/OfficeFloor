package net.officefloor.spring.starter.rest;

public interface OfficeFloorSpringBootExtension {

    /**
     * Extends {@link net.officefloor.frame.api.manage.OfficeFloor} Spring Boot support.
     *
     * @param context {@link OfficeFloorSpringBootExtensionContext}.
     * @throws Exception If fails to extend support.
     */
    void extendSpringBootSupport(OfficeFloorSpringBootExtensionContext context) throws Exception;

}