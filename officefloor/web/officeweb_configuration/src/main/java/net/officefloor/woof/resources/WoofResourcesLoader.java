package net.officefloor.woof.resources;

import net.officefloor.woof.model.resources.WoofResourcesModel;

/**
 * Loads the {@link WoofResourcesModel} configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofResourcesLoader {

	/**
	 * Loads the {@link WoofResourcesModel} configuration.
	 * 
	 * @param context
	 *            {@link WoofResourcesLoaderContext}.
	 * @throws Exception
	 *             If fails to load the configuration.
	 */
	void loadWoofResourcesConfiguration(WoofResourcesLoaderContext context) throws Exception;

}