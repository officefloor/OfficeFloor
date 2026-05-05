package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.SectionInput;

/**
 * Builds the composed {@link net.officefloor.frame.api.function.ManagedFunction} instances.
 */
public interface ComposeArchitect {

    /**
     * Adds {@link OfficeSectionInput} that will be available to link in composition via <code>#inputName</code>.
     *
     * @param inputName Name for input in composition.
     * @param input     {@link OfficeSectionInput}.
     */
    void addInput(String inputName, OfficeSectionInput input);

    /**
     * Adds {@link OfficeGovernance} for the composition.
     *
     * @param governanceName Name used in composition for the {@link OfficeGovernance}.
     * @param goverance      {@link OfficeGovernance}.
     */
    void addGovernance(String governanceName, OfficeGovernance goverance);

    /**
     * Builds the item requiring composition.
     *
     * @param sectionName   Name of the {@link net.officefloor.compile.spi.office.OfficeSection} to contain the composition.
     * @param source        {@link ComposeSource} to source the item requiring composition.
     * @param resourceName  Name of resource defining the composition.
     * @param properties    {@link PropertyList} to configure the composition.
     * @param configuration {@link Class} extending {@link ComposeConfiguration} to provide additional configuration for the item being built.
     * @param <C>           Configuration type.
     * @param <T>           Built item type.
     * @return Built item.
     * @throws Exception If fails to build item.
     */
    <C extends ComposeConfiguration, T> T addComposition(String sectionName, ComposeSource<T, C> source,
                                                         String resourceName, PropertyList properties,
                                                         Class<C> configuration) throws Exception;

    /**
     * Builds a directory of items requiring composition.
     *
     * @param composer          {@link DirectoryItemComposer} to source the items requiring composition.
     * @param resourceDirectory Name of directory containing the compositions.
     * @param properties        {@link PropertyList} to configure the compositions.
     * @param listener          {@link ComposeListener} to receive the built items.
     * @param <T>               Build item type.
     * @throws Exception If fails to build the items.
     */
    <T> void addCompositions(DirectoryItemComposer<T> composer, String resourceDirectory,
                             PropertyList properties, ComposeListener<T> listener) throws Exception;

}
