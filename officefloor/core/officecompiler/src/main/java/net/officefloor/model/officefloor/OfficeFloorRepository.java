package net.officefloor.model.officefloor;

import java.io.IOException;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository of the {@link OfficeFloorModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorRepository {

	/**
	 * Retrieves the {@link OfficeFloorModel} from {@link ConfigurationItem}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloorModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link OfficeFloorModel}.
	 * @throws IOException
	 *             If fails to retrieve the {@link OfficeFloorModel}.
	 */
	void retrieveOfficeFloor(OfficeFloorModel officeFloor, ConfigurationItem configuration) throws IOException;

	/**
	 * Stores the {@link OfficeFloorModel} into the {@link ConfigurationItem}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloorModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the
	 *            {@link OfficeFloorModel}.
	 * @throws IOException
	 *             If fails to store the {@link OfficeFloorModel}.
	 */
	void storeOfficeFloor(OfficeFloorModel officeFloor, WritableConfigurationItem configuration) throws IOException;

}