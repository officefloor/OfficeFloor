package net.officefloor.woof.model.woof;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.woof.model.woof.WoofModel;

/**
 * Repository for obtaining the WoOF (Web on OfficeFloor) model.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofRepository {

	/**
	 * Retrieves the {@link WoofModel} from the {@link ConfigurationItem}.
	 * 
	 * @param woof
	 *            {@link WoofModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link WoofModel}.
	 */
	void retrieveWoof(WoofModel woof, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link WoofModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param woof
	 *            {@link WoofModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link WoofModel}.
	 */
	void storeWoof(WoofModel woof, WritableConfigurationItem configuration) throws Exception;

}