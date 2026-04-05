package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.ComposeConfig;
import net.officefloor.compile.properties.PropertyList;

/**
 * Builds an item requiring composition.
 */
public interface ComposeBuilder {

    /**
     * Builds the item.
     *
     * @param sectionName   Name of the {@link net.officefloor.compile.spi.office.OfficeSection} to contain the composition.
     * @param source        {@link ComposeSource} to source the item requiring composition.
     * @param resourceName  Name of resource defining the composition.
     * @param properties    {@link PropertyList} to configure the composition.
     * @param configuration {@link Class} extending {@link ComposeConfig} to provide additional configuration for the item being built.
     * @param <C>           Configuration type.
     * @param <T>           Built item type.
     * @return Built item.
     * @throws Exception If fails to build item.
     */
    <C extends ComposeConfig, T> T build(String sectionName, ComposeSource<T, C> source,
                                         String resourceName, PropertyList properties,
                                         Class<C> configuration) throws Exception;

}
