package net.officefloor.model.section;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository of {@link SectionModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionRepository {

	/**
	 * Retrieves the {@link SectionModel} from the {@link ConfigurationItem}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link SectionModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link SectionModel}.
	 */
	void retrieveSection(SectionModel section, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link SectionModel} into the {@link ConfigurationItem}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the
	 *            {@link SectionModel}.
	 * @throws Exception
	 *             If fails to store the {@link SectionModel}.
	 */
	void storeSection(SectionModel section, WritableConfigurationItem configuration) throws Exception;

}