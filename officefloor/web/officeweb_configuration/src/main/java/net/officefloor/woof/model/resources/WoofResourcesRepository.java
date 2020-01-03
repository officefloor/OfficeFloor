package net.officefloor.woof.model.resources;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository for obtaining the {@link WoofResourcesModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofResourcesRepository {

	/**
	 * Retrieves the {@link WoofResourcesModel} from the
	 * {@link ConfigurationItem}.
	 * 
	 * @param resources
	 *            {@link WoofResourcesModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link WoofResourcesModel}.
	 */
	void retrieveWoofResources(WoofResourcesModel resources, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link WoofResourcesModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param resources
	 *            {@link WoofResourcesModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link WoofResourcesModel}.
	 */
	void storeWoofResources(WoofResourcesModel resources, WritableConfigurationItem configuration) throws Exception;

}