package net.officefloor.model.repository;

import java.io.IOException;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.Model;

/**
 * Repository to the {@link Model} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ModelRepository {

	/**
	 * Configures the {@link Model} from the {@link ConfigurationItem}.
	 * 
	 * @param model
	 *            {@link Model} to be configured.
	 * @param configuration
	 *            {@link ConfigurationItem} containing configuration of the
	 *            {@link Model}.
	 * @throws IOException
	 *             If fails to configure the {@link Model}.
	 */
	void retrieve(Object model, ConfigurationItem configuration) throws IOException;

	/**
	 * Stores the {@link Model} within the {@link WritableConfigurationItem}.
	 * 
	 * @param model
	 *            {@link Model} to be stored.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the {@link Model}.
	 * @throws IOException
	 *             If fails to store the {@link Model}.
	 */
	void store(Object model, WritableConfigurationItem configuration) throws IOException;

}