package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.ComposeConfig;

/**
 * Source to build composition.
 */
public interface ComposeSource<T, C extends ComposeConfig> {

    T source(ComposeContext<C> context) throws Exception;

}
