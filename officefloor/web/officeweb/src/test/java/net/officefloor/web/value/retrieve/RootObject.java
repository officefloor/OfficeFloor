package net.officefloor.web.value.retrieve;


/**
 * <p>
 * Root object type.
 * <p>
 * Provides methods for testing.
 * 
 * @author Daniel Sagenschneider
 */
public interface RootObject {

	/**
	 * Obtains String value for simple property name.
	 * 
	 * @return String value as per testing.
	 */
	String getValue();

	/**
	 * Obtains an object for <code>property.text</code> property names.
	 * 
	 * @return {@link PropertyObject} as per testing.
	 */
	PropertyObject getProperty();

}