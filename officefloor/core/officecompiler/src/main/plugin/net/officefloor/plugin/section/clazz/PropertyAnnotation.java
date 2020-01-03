package net.officefloor.plugin.section.clazz;

/**
 * {@link Property} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyAnnotation {

	/**
	 * Name of the property.
	 */
	private final String name;

	/**
	 * Value of the property.
	 */
	private final String value;

	/**
	 * Instantiate.
	 * 
	 * @param name  Name of the property.
	 * @param value Value of the property.
	 */
	public PropertyAnnotation(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Instantiate.
	 * 
	 * @param property {@link Property}.
	 */
	public PropertyAnnotation(Property property) {
		this(property.name(),
				Void.class.equals(property.valueClass()) ? property.valueClass().getName() : property.value());
	}

	/**
	 * Obtains the name of the property.
	 * 
	 * @return Name of the property.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the value of the property.
	 * 
	 * @return Value of the property.
	 */
	public String getValue() {
		return this.value;
	}

}