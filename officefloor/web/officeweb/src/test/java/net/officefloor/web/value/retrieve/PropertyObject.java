package net.officefloor.web.value.retrieve;

import net.officefloor.web.HttpPathParameter;

/**
 * <p>
 * Property object type.
 * <p>
 * Provides methods for testing.
 * 
 * @author Daniel Sagenschneider
 */
public interface PropertyObject {

	/**
	 * Allows for <code>property.text</code> property name.
	 * 
	 * @return String value as per testing.
	 */
	@HttpPathParameter("test")
	String getText();

	/**
	 * Allows for <code>property.recursive.recursive.(etc)</code> property name.
	 * 
	 * @return String value as per testing.
	 */
	PropertyObject getRecursive();

}