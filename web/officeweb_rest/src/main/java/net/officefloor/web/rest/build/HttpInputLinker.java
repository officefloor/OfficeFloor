package net.officefloor.web.rest.build;

/**
 * Overrides the default linking of the {@link net.officefloor.web.build.HttpInput} to its service
 * {@link net.officefloor.compile.spi.office.OfficeSectionInput}. Allows intercepting logic to be
 * inserted before the {@link RestMethod} is invoked.
 */
@FunctionalInterface
public interface HttpInputLinker {

    /**
     * Links the {@link net.officefloor.web.build.HttpInput} to the service section,
     * optionally inserting intermediate logic.
     *
     * @param context {@link HttpInputLinkerContext}.
     */
    void link(HttpInputLinkerContext context);

}
