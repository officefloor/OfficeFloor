package net.officefloor.activity.compose.build;

import net.officefloor.compile.properties.PropertyList;

/**
 * Builds the composed {@link net.officefloor.frame.api.function.ManagedFunction} instances.
 */
public interface ComposeArchitect<S> {

    /**
     * Adds a composition.
     *
     * @param sectionName Name to identify the resulting {@link net.officefloor.compile.spi.office.OfficeSection}.
     * @param resourceName Name of resource defining the composition.
     * @param properties {@link PropertyList} to configure the composition.
     * @return {@link net.officefloor.compile.spi.office.OfficeSection} or {@link net.officefloor.compile.spi.section.SubSection}.
     */
    S addComposition(String sectionName, String resourceName, PropertyList properties);

}
