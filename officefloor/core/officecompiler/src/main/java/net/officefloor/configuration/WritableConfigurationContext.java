package net.officefloor.configuration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Writable {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WritableConfigurationContext extends ConfigurationContext {

	/**
	 * Obtains the {@link WritableConfigurationItem} at the location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to obtain.
	 * @return {@link WritableConfigurationItem}.
	 * @throws IOException
	 *             If can not obtain a {@link WritableConfigurationItem} at the
	 *             location.
	 */
	WritableConfigurationItem getWritableConfigurationItem(String location) throws IOException;

	/**
	 * Creates a new {@link WritableConfigurationItem} at the relative location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to create.
	 * @param configuration
	 *            Configuration for the {@link WritableConfigurationItem}.
	 * @return The created {@link WritableConfigurationItem}.
	 * @throws IOException
	 *             If fails to create the {@link WritableConfigurationItem}.
	 */
	WritableConfigurationItem createConfigurationItem(String location, InputStream configuration) throws IOException;

	/**
	 * Deletes the {@link WritableConfigurationItem} at the relative location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to delete.
	 * @throws IOException
	 *             If can not delete the {@link WritableConfigurationItem} at
	 *             the relative location.
	 */
	void deleteConfigurationItem(String location) throws IOException;

}