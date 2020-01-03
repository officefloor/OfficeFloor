package net.officefloor.woof.model.objects;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.woof.model.objects.WoofObjectsModel;

/**
 * Repository for obtaining the {@link WoofObjectsModel} for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofObjectsRepository {

	/**
	 * Retrieves the {@link WoofObjectsModel} from the
	 * {@link ConfigurationItem}.
	 * 
	 * @param objects
	 *            {@link WoofObjectsModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link WoofObjectsModel}.
	 */
	void retrieveWoofObjects(WoofObjectsModel objects, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link WoofObjectsModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param objects
	 *            {@link WoofObjectsModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link WoofObjectsModel}.
	 */
	void storeWoofObjects(WoofObjectsModel objects, WritableConfigurationItem configuration) throws Exception;

}