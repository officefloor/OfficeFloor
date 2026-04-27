package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.ComposeConfiguration;

/**
 * Source to build composition.
 */
public interface ComposeSource<T, C extends ComposeConfiguration> {

    /**
     * Sources the item from the composition.
     *
     * @param context {@link ComposeContext}.
     * @return Item from composition.
     * @throws Exception If fails to source item.
     */
    T source(ComposeContext<C> context) throws Exception;

}
