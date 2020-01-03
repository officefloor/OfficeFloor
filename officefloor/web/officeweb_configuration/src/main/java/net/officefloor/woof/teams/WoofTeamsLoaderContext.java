package net.officefloor.woof.teams;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.woof.resources.WoofResourcesLoader;

/**
 * Context for the {@link WoofResourcesLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsLoaderContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration of the
	 * teams.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration of the
	 *         objects.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link OfficeFloorDeployer} to be configured with the teams.
	 * 
	 * @return {@link OfficeFloorDeployer} to be configured with the objects.
	 */
	OfficeFloorDeployer getOfficeFloorDeployer();

	/**
	 * Obtains the {@link OfficeFloorExtensionContext}.
	 * 
	 * @return {@link OfficeFloorExtensionContext}.
	 */
	OfficeFloorExtensionContext getOfficeFloorExtensionContext();

	/**
	 * Obtains the {@link DeployedOffice} that the WoOF application is being
	 * deployed into.
	 * 
	 * @return {@link DeployedOffice} that the WoOF application is being deployed
	 *         into.
	 */
	DeployedOffice getDeployedOffice();

}