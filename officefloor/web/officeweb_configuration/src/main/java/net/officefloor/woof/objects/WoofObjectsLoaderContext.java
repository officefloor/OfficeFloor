package net.officefloor.woof.objects;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;

/**
 * Context for the {@link WoofObjectsLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofObjectsLoaderContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration of the
	 * objects.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration of the
	 *         objects.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link OfficeArchitect} to be configured with the objects.
	 * 
	 * @return {@link OfficeArchitect} to be configured with the objects.
	 */
	OfficeArchitect getOfficeArchitect();

	/**
	 * Obtains the {@link OfficeExtensionContext}.
	 * 
	 * @return {@link OfficeExtensionContext}.
	 */
	OfficeExtensionContext getOfficeExtensionContext();

}