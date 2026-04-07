package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.ComposeConfiguration;

/**
 * Source to build composition.
 */
public interface ComposeSource<T, C extends ComposeConfiguration> {

    T source(ComposeContext<C> context) throws Exception;

}
