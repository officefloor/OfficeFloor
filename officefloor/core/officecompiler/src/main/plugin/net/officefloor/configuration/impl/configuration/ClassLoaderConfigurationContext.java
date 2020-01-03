package net.officefloor.configuration.impl.configuration;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.impl.ConfigurationContextImpl;

/**
 * {@link ConfigurationContext} for a {@link ClassLoader} class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassLoaderConfigurationContext extends ConfigurationContextImpl {

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param properties
	 *            {@link PropertyList}. May be <code>null</code>.
	 */
	public ClassLoaderConfigurationContext(ClassLoader classLoader, PropertyList properties) {
		super((location) -> classLoader.getResourceAsStream(location), new PropertyListSourceProperties(properties));
	}

}