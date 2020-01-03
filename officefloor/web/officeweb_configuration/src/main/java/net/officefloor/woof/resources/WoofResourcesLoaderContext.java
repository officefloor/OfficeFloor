package net.officefloor.woof.resources;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.web.resource.build.HttpResourceArchitect;

/**
 * Context for the {@link WoofResourcesLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofResourcesLoaderContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration of the
	 * resources.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration of the
	 *         resources.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link HttpResourceArchitect}.
	 * 
	 * @return {@link HttpResourceArchitect}.
	 */
	HttpResourceArchitect getHttpResourceArchitect();

	/**
	 * Obtains the {@link OfficeArchitect}.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

	/**
	 * Obtains the {@link OfficeExtensionContext}.
	 * 
	 * @return {@link OfficeExtensionContext}.
	 */
	OfficeExtensionContext getOfficeExtensionContext();

}