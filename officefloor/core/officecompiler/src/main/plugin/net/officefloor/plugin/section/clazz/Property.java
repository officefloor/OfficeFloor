package net.officefloor.plugin.section.clazz;

/**
 * Property annotation.
 * 
 * @author Daniel Sagenschneider
 */
public @interface Property {

	/**
	 * Obtains the name of the property.
	 * 
	 * @return Name of the property.
	 */
	String name();

	/**
	 * Obtains the value of the property.
	 * 
	 * @return Value of the property.
	 */
	String value() default "";

	/**
	 * <p>
	 * Obtains the value as a {@link Class}.
	 * <p>
	 * This simplifies configuring fully qualified class names as the property
	 * value.
	 * 
	 * @return Value as a {@link Class}.
	 */
	Class<?> valueClass() default Void.class;

}