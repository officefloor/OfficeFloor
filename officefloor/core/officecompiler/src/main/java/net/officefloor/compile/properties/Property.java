package net.officefloor.compile.properties;

/**
 * Property.
 * 
 * @author Daniel Sagenschneider
 */
public interface Property {

	/**
	 * Obtains the display label for the property.
	 * 
	 * @return Display label for the property.
	 */
	String getLabel();

	/**
	 * Obtains the name of the property.
	 * 
	 * @return Name of the property.
	 */
	String getName();

	/**
	 * Obtains the value of the property.
	 * 
	 * @return Value of the property.
	 */
	String getValue();

	/**
	 * Changes the value of the property.
	 * 
	 * @param value
	 *            Value of the property.
	 */
	void setValue(String value);

}