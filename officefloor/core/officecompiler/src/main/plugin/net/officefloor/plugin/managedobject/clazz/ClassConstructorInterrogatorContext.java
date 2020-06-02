package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Constructor;

/**
 * Context for the {@link ClassConstructorInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassConstructorInterrogatorContext {

	/**
	 * Obtains the {@link Class} of object to be constructed for dependency
	 * injection.
	 * 
	 * @return {@link Class} of object to be constructed for dependency injection.
	 */
	Class<?> getObjectClass();

	/**
	 * <p>
	 * Provides optional error information about why a {@link Constructor} was not
	 * found.
	 * <p>
	 * This provides improved feedback to help resolve issue of why a
	 * {@link Constructor} was not selected in interrogation.
	 * 
	 * @param errorInformation Error information.
	 */
	void setErrorInformation(String errorInformation);

}