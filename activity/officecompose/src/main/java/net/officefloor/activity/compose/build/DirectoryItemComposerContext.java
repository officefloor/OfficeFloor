package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.ComposeConfiguration;

/**
 * Context for the {@link DirectoryItemComposer}.
 */
public interface DirectoryItemComposerContext {

    /**
     * Obtains the name of the directory item.
     *
     * @return Name of the directory item.
     */
    String getItemName();

    /**
     * <p>
     * Obtains the configuration for the directory item.
     * <p>
     * All directory items may not be compositions, so this enables loading non-composition configuration.
     *
     * @param type Type of configuration.
     * @param <C>  Type of configuration.
     * @return Configuration.
     */
    <C> C getConfiguration(Class<C> type);

    /**
     * Adds a composition for this directory item.
     *
     * @param sectionName       Name of the {@link net.officefloor.compile.spi.office.OfficeSection} to contain
     *                          the composition.
     * @param source            {@link ComposeSource} to source the items requiring composition.
     * @param configurationType {@link Class} extending {@link ComposeConfiguration} to provide additional configuration for the item being built.
     * @param <I>               Item type.
     * @param <C>               Configuration type.
     * @return Composition item.
     */
    <I, C extends ComposeConfiguration> I addComposition(String sectionName, ComposeSource<I, C> source, Class<C> configurationType);

}
