package net.officefloor.compile.properties;

/**
 * Item that is configurable with {@link Property} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface PropertyConfigurable {

	/**
	 * Adds a {@link Property} to configure the item.
	 * 
	 * @param name
	 *            Name of {@link Property}.
	 * @param value
	 *            Value of {@link Property}.
	 */
	void addProperty(String name, String value);

}