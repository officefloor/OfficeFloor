package net.officefloor.model.office;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository of the {@link OfficeModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeRepository {

	/**
	 * Retrieves the {@link OfficeModel} from the {@link ConfigurationItem}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link OfficeModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link OfficeModel}.
	 */
	void retrieveOffice(OfficeModel office, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link OfficeModel} into the {@link ConfigurationItem}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the
	 *            {@link OfficeModel}.
	 * @throws Exception
	 *             If fails to store the {@link OfficeModel}.
	 */
	void storeOffice(OfficeModel office, WritableConfigurationItem configuration) throws Exception;

}