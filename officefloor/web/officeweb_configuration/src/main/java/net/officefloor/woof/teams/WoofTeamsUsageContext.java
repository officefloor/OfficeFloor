package net.officefloor.woof.teams;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.woof.resources.WoofResourcesLoader;

/**
 * Context for the {@link WoofResourcesLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsUsageContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration of the
	 * teams.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link OfficeArchitect} to be configured with the teams.
	 * 
	 * @return {@link OfficeArchitect} to be configured.
	 */
	OfficeArchitect getOfficeArchitect();

	/**
	 * Obtains the {@link OfficeExtensionContext}.
	 * 
	 * @return {@link OfficeExtensionContext}.
	 */
	OfficeExtensionContext getOfficeExtensionContext();

}