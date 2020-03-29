package net.officefloor.servlet.inject;

import java.lang.reflect.Field;

/**
 * Field to have dependency injected.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectField {

	/**
	 * {@link Field} to load the dependency.
	 */
	public final Field field;

	/**
	 * Dependency to inject.
	 */
	public final Object dependency;

	/**
	 * Instantiate.
	 * 
	 * @param field      {@link Field} to load the dependency.
	 * @param dependency Dependency to inject.
	 */
	InjectField(Field field, Object dependency) {
		this.field = field;
		this.dependency = dependency;
	}
}