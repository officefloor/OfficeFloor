package net.officefloor.frame.api.source;

import java.util.Properties;

/**
 * Properties for the source.
 * 
 * @author Daniel Sagenschneider
 */
public interface SourceProperties {

	/**
	 * <p>
	 * Obtains the names of the available properties in the order they were
	 * defined. This allows for ability to provide variable number of properties
	 * identified by a naming convention and being able to maintain their order.
	 * <p>
	 * An example would be providing a listing of routing configurations, each
	 * entry named <code>route.[something]</code> and order indicating priority.
	 * 
	 * @return Names of the properties in the order defined.
	 */
	String[] getPropertyNames();

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws UnknownPropertyError
	 *             If property was not configured. Let this propagate as
	 *             OfficeFloor will handle it.
	 */
	String getProperty(String name) throws UnknownPropertyError;

	/**
	 * Obtains the property value or subsequently the default value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param defaultValue
	 *            Default value if property not specified.
	 * @return Value of the property or the the default value if not specified.
	 */
	String getProperty(String name, String defaultValue);

	/**
	 * Properties to configure the source.
	 * 
	 * @return Properties specific for the source.
	 */
	Properties getProperties();

}