package net.officefloor.activity.model;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository for obtaining the Activity model.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActivityRepository {

	/**
	 * Retrieves the {@link ActivityModel} from the {@link ConfigurationItem}.
	 * 
	 * @param activity      {@link ActivityModel}.
	 * @param configuration {@link ConfigurationItem}.
	 * @throws Exception If fails to retrieve the {@link ActivityModel}.
	 */
	void retrieveActivity(ActivityModel activity, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link ActivityModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param activity      {@link ActivityModel}.
	 * @param configuration {@link WritableConfigurationItem}.
	 * @throws Exception If fails to store the {@link ActivityModel}.
	 */
	void storeActivity(ActivityModel activity, WritableConfigurationItem configuration) throws Exception;

}