package net.officefloor.activity.compose.build;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionInput;

/**
 * Builds the composed {@link net.officefloor.frame.api.function.ManagedFunction} instances.
 */
public interface ComposeArchitect<S> {

    /**
     * {@link SectionInput} name to invoke the composition.
     */
    public static final String INPUT_NAME = "procedure";

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
