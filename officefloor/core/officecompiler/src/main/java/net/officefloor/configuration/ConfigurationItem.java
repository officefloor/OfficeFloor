package net.officefloor.configuration;

import java.io.InputStream;
import java.io.Reader;

/**
 * Item of configuration within a {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConfigurationItem {

	/**
	 * Obtains the {@link Reader} to the configuration that this represents.
	 * 
	 * @return {@link Reader} to the configuration.
	 * @throws ConfigurationError
	 *             Let this propagate to let OfficeFloor handle failure in
	 *             loading {@link ConfigurationItem}.
	 */
	Reader getReader() throws ConfigurationError;

	/**
	 * Obtains {@link InputStream} to the configuration that this represents.
	 * 
	 * @return {@link InputStream} to the configuration.
	 * @throws ConfigurationError
	 *             Let this propagate to let OfficeFloor handle failure in
	 *             loading {@link ConfigurationItem}.
	 */
	InputStream getInputStream() throws ConfigurationError;

}