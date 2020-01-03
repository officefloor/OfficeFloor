package net.officefloor.configuration;

import java.io.IOException;
import java.io.InputStream;

import net.officefloor.compile.properties.PropertyList;

/**
 * Writable {@link ConfigurationItem}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WritableConfigurationItem extends ConfigurationItem {

	/**
	 * Obtains the {@link InputStream} to the raw configuration (no
	 * {@link PropertyList} replacement).
	 * 
	 * @return {@link InputStream} to the raw configuration.
	 * @throws IOException
	 *             If fails to load the raw configuration.
	 */
	InputStream getRawConfiguration() throws IOException;

	/**
	 * Specifies the configuration that this is to represent.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @throws IOException
	 *             If fails to set the configuration.
	 */
	void setConfiguration(InputStream configuration) throws IOException;

	/**
	 * Obtains the {@link WritableConfigurationContext} for this
	 * {@link WritableConfigurationItem}.
	 * 
	 * @return {@link WritableConfigurationContext} for this
	 *         {@link WritableConfigurationItem}.
	 */
	WritableConfigurationContext getContext();

}